package com.mediatek.mt6381eco.biz.peripheral;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.mediatek.blenativewrapper.DiscoverPeripheral;
import com.mediatek.blenativewrapper.StateInfo;
import com.mediatek.blenativewrapper.rxbus.RxBus;
import com.mediatek.blenativewrapper.utils.RxQueueTaskExecutor;
import com.mediatek.mt6381.ble.MT6381Peripheral;
import com.mediatek.mt6381.ble.command.AskSystemInfoCommand;
import com.mediatek.mt6381.ble.command.AskTemperatureCommand;
import com.mediatek.mt6381.ble.command.MeasureFinishCommand;
import com.mediatek.mt6381.ble.command.MeasurementCommand;
import com.mediatek.mt6381.ble.command.SetDeviceNameCommand;
import com.mediatek.mt6381.ble.command.TemperatureCalibrationCommand;
import com.mediatek.mt6381.ble.data.SystemInformationData;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import java.util.UUID;
import timber.log.Timber;

public class PeripheralService extends Service {
  public static final String INTENT_PERIPHERAL = "PERIPHERAL";
  private final IBinder mBinder = new LocalBinder();
  //modify static by herman
  public static  MT6381Peripheral mMt6381Peripheral;
  private final CompositeDisposable mDisposables = new CompositeDisposable();
  private final RxQueueTaskExecutor mQueue = new RxQueueTaskExecutor();
  private DiscoverPeripheral mCurrentPeripheral;

  @Override public void onCreate() {
    super.onCreate();
    Timber.i("onCreate");
    mMt6381Peripheral = new MT6381Peripheral(getApplicationContext());
  }

  //add by herman
/*  public static MT6381Peripheral getmMt6381Peripheral() {
    return mMt6381Peripheral;
  }*/

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    Timber.i("onStartCommand");
    if (intent != null) {
      DiscoverPeripheral discoverPeripheral = intent.getParcelableExtra(INTENT_PERIPHERAL);
      if (discoverPeripheral != null) {
        if (!discoverPeripheral.getAddress().equals(mMt6381Peripheral.getAddress())) {
          if (mMt6381Peripheral.getStateInfo().isConnected()) {
            disconnect();
          }
        }
        mCurrentPeripheral = discoverPeripheral;
        subscribe(mMt6381Peripheral.changePeripheral(discoverPeripheral), "change_peripheral");
      }
    }
    com.mediatek.mt6381eco.rxbus.RxBus.getInstance().post(new ServiceStartedEvent());
    return START_REDELIVER_INTENT;
  }

  @Override public void onDestroy() {
    super.onDestroy();
    Timber.i("onDestroy");
    mMt6381Peripheral.destroy();
    mDisposables.clear();
  }

  @Nullable @Override public IBinder onBind(Intent intent) {
    return mBinder;
  }

  private int map(StateInfo.ConnectionState state) {
    switch (state) {
      case Disconnecting:
        return IPeripheral.STATE_DISCONNECTING;
      case Connecting:
        return IPeripheral.STATE_CONNECTEDING;
      case Connected:
        return IPeripheral.STATE_CONNECTED;
      default:
        return IPeripheral.STATE_DISCONNECTED;
    }
  }

  private void disconnect() {
    subscribe(mQueue.queue(mMt6381Peripheral.disconnect()), "disconnect");
  }

  private void subscribe(Completable completable, String logMessage) {
    completable.subscribe(() -> Timber.i(logMessage), throwable -> Timber.e(throwable, logMessage));
  }

  public class LocalBinder extends Binder implements IPeripheral, IBlePeripheral {

    @Override public Completable connect() {
      Timber.d("connect");
      return mQueue.queue(Completable.defer(() -> {
        if (getConnectionState() == IPeripheral.STATE_CONNECTED) {
          return Completable.complete();
        }
        return mMt6381Peripheral.connect();
      }));
    }

    @Override public void disconnect() {
      PeripheralService.this.disconnect();
    }

    @Override public int getConnectionState() {
      return map(mMt6381Peripheral.getStateInfo().getConnectionState());
    }

    @Override public Flowable<Integer> onConnectionChange() {
      return mMt6381Peripheral.onConnectionStateChange()
          .doOnNext(state -> Timber.d("onConnectionChange:%s", state.name()))
          .map(PeripheralService.this::map);
    }

    @Override public Completable startMeasure(boolean downSample) {
      return mMt6381Peripheral.sendCommand(MeasurementCommand.createOn(downSample));
    }

    @Override public Completable stopMeasure() {
      return mMt6381Peripheral.sendCommand(MeasurementCommand.createOff());
    }

    @Override public Completable startThroughputTest() {
      return mMt6381Peripheral.sendCommand(MeasurementCommand.createThroughputOn());
    }

    @Override public long getThroughput() {
      return mMt6381Peripheral.getBps();
    }

    @Override public Single<DeviceInfo> readDeviceInfo() {
      Flowable<DeviceInfo> sendCommand = mMt6381Peripheral.sendCommand(new AskSystemInfoCommand())
          .concatWith(mMt6381Peripheral.readSystemInfo())
          .toFlowable();
      return RxBus.getInstance()
          .toFlowable(SystemInformationData.class)
          .take(1)
          .map(systemInformationData -> new DeviceInfo(systemInformationData.getThroughputBps(),
              systemInformationData.getMBattery(), systemInformationData.getMFirmware()))
          .mergeWith(sendCommand)
          .singleOrError();
    }

    @Override public String getName() {
      if (mCurrentPeripheral != null) {
        return mCurrentPeripheral.getLocalName();
      }
      return "";
    }

    @Override public Completable setDeviceName(String deviceName) {
      return mMt6381Peripheral.sendCommand(new SetDeviceNameCommand(deviceName));
    }

    //add by herman for temperature
    @Override public Completable readTemperature() {
      Log.d("PeripheralService","readTemperature and setCommand");
      return mMt6381Peripheral.sendCommand(new AskTemperatureCommand());
    }
    //end

    //add by herman for temperature
    @Override public Completable calibrationTemperature() {
      Log.d("PeripheralService","readTemperature and setCommand");
      return mMt6381Peripheral.sendCommand(new TemperatureCalibrationCommand());
    }
    //end

    //add by herman
    @Override public Completable sendMeasureFinish() {
      Log.d("PeripheralService","readTemperature and setCommand");
      return mMt6381Peripheral.sendCommand(new MeasureFinishCommand());
    }
    //end

    @Override public Completable writeCharacteristic(UUID uuid, boolean noResponse, byte[] data) {
      return mMt6381Peripheral.writeCharacteristic(mMt6381Peripheral.getCharacteristic(uuid),
          noResponse, data);
    }
  }
}
