package com.mediatek.mt6381eco.biz.peripheral;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.Nullable;
import com.mediatek.mt6381eco.dagger.SupportSensorTypes;
import com.mediatek.mt6381eco.rxbus.RxBus;
import dagger.android.AndroidInjection;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import timber.log.Timber;

public class PeripheralService extends Service implements SensorEventListener {
  private final IBinder mBinder = new LocalBinder();
  private SensorManager mSensorManager;
  private HandlerThread mWorkHandler;
  @Inject SupportSensorTypes mSupportSensorTypes;

  @Override public void onCreate() {
    super.onCreate();
    AndroidInjection.inject(this);
    Timber.i("onCreate");
    mSensorManager = (SensorManager) getApplication().getSystemService(Context.SENSOR_SERVICE);
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    Timber.i("onStartCommand");
    return START_REDELIVER_INTENT;
  }

  @Override public void onDestroy() {
    super.onDestroy();
    Timber.i("onDestroy");
    quitHandler();
  }

  @Nullable @Override public IBinder onBind(Intent intent) {
    return mBinder;
  }

  private void quitHandler() {
    if (mWorkHandler != null) {
      mWorkHandler.quit();
      mWorkHandler = null;
    }
  }

  @Override public void onSensorChanged(SensorEvent event) {
    int type = -1;
    switch (event.sensor.getName()) {
      case SupportSensorTypes.STRING_TYPE_EKG: {
        type = SensorData.DATA_TYPE_EKG;
        break;
      }
      case SupportSensorTypes.STRING_TYPE_PPG1: {
        type = SensorData.DATA_TYPE_PPG1;
        break;
      }
      case SupportSensorTypes.STRING_TYPE_PPG2: {
        type = SensorData.DATA_TYPE_PPG2;
        break;
      }
      default:
        return;
    }
    postSensorData(type, (int) event.values[1], (int) event.values[0], (int) event.values[4]);

    if (type == SensorData.DATA_TYPE_PPG1) {
      postSensorData(SensorData.DATA_TYPE_AMB1, (int) event.values[1], (int) event.values[2],
          (int) event.values[4]);
      postSensorData(SensorData.DATA_TYPE_LED_SETTING, (int) event.values[1], (int) event.values[3],
          (int) event.values[4]);
    }
  }

  private void postSensorData(int type, int sn, int value, int status) {
    SensorData sensorData = new SensorData();
    sensorData.type = type;
    sensorData.sn = sn;
    sensorData.value = value;
    sensorData.status = status;
    RxBus.getInstance().post(sensorData);
  }

  private void registerSensorListener(String[] sensorArray) {
    quitHandler();
    mWorkHandler = new HandlerThread(this.getClass().getSimpleName());
    mWorkHandler.start();
    Handler handler = new Handler(mWorkHandler.getLooper());
    List<String> mt6381SensorList = Arrays.asList(sensorArray);
    List<Sensor> supportSensorList = mSensorManager.getSensorList(Sensor.TYPE_ALL);
    for (Sensor sensor : supportSensorList) {
      if (mt6381SensorList.contains(sensor.getName())) {
        mSensorManager.registerListener(this, sensor, 1953, 0, handler);
      }
    }
  }

  @Override public void onAccuracyChanged(Sensor sensor, int accuracy) {

  }

  public class LocalBinder extends Binder implements IPeripheral {

    @Override public Completable connect() {
      return Completable.complete();
    }

    @Override public void disconnect() {

    }

    @Override public int getConnectionState() {
      return IPeripheral.STATE_CONNECTED;
    }

    @Override public Flowable<Integer> onConnectionChange() {
      return Flowable.never();
    }

    @Override public Completable startMeasure(boolean downSample) {
      return Completable.create(e -> {
        registerSensorListener(mSupportSensorTypes.getTypeStringArray());
        e.onComplete();
      });
    }

    @Override public Completable stopMeasure() {
      return Completable.create(e -> {
        mSensorManager.unregisterListener(PeripheralService.this);
        e.onComplete();
        quitHandler();
      });
    }

    @Override public Completable startThroughputTest() {
      return Completable.complete();
    }

    @Override public long getThroughput() {
      return Long.MAX_VALUE;
    }

    @Override public Single<DeviceInfo> readDeviceInfo() {
      return Single.just(new DeviceInfo(Integer.MAX_VALUE, 100, "0.0"));
    }
  }
}
