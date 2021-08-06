package com.mediatek.mt6381.ble;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.mediatek.blenativewrapper.BLEDataReceiver;
import com.mediatek.blenativewrapper.BlePeripheralSettings;
import com.mediatek.blenativewrapper.DiscoverPeripheral;
import com.mediatek.blenativewrapper.GattConnectPriority;
import com.mediatek.blenativewrapper.RxBlePeripheral;
import com.mediatek.blenativewrapper.StateInfo;
import com.mediatek.blenativewrapper.rxbus.RxBus;
import com.mediatek.blenativewrapper.utils.MeasureSpeed;
import com.mediatek.blenativewrapper.utils.RxQueueTaskExecutor;
import com.mediatek.mt6381.ble.command.AskTemperatureCommand;
import com.mediatek.mt6381.ble.command.BaseCommand;
import com.mediatek.mt6381.ble.command.SetTimeoutCommand;
import com.mediatek.mt6381.ble.data.CommandResponse;
import com.mediatek.mt6381.ble.events.CommandCompleteEvent;
import com.mediatek.mt6381.ble.events.CommandErrorEvent;
import com.mediatek.mt6381.ble.exceptions.CommandException;
import io.reactivex.Completable;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import timber.log.Timber;

public class MT6381Peripheral extends RxBlePeripheral {
  private static final int MTU_SIZE = 250;
  private static final int TIMEOUT_COMMAND = 10000;
  private final MeasureSpeed mMeasureSpeed = new MeasureSpeed(2000, 50);
  private final RxQueueTaskExecutor mQueueTaskExecutor = new RxQueueTaskExecutor();

  public MT6381Peripheral(Context applicationContext, DiscoverPeripheral device) {
    this(applicationContext);
    changePeripheral(device).onErrorComplete().subscribe();
  }

  public MT6381Peripheral(Context context) {
    super(context, getSetting(), new MT6381DataReceiver());
  }

  private static Bundle getSetting() {
    Bundle bundle = new Bundle();
    bundle.putInt(BlePeripheralSettings.Key.MtuSize.name(), MTU_SIZE);
    bundle.putBoolean(BlePeripheralSettings.Key.UseCreateBond.name(), false);
    bundle.putBoolean(BlePeripheralSettings.Key.UseRemoveBond.name(), true);
    return bundle;
  }

  public Completable sendCommand(BaseCommand baseCommand) {
    Log.d("MT6381Peripheral","sendCommand: " + baseCommand.getType());
    Completable cpResponse = RxBus.getInstance()
        .toFlowable(CommandResponse.class)
        .firstOrError()
        .doOnSuccess(commandResponse -> {
          if (commandResponse.getCommandType() == baseCommand.getType() && commandResponse.isSuccess()) {
            Log.d("MT6381Peripheral","sendCommand success baseCommand.getType()" + baseCommand.getType());
          } else {
            Log.d("MT6381Peripheral","error_response");
            throw new CommandException("error_response:" + commandResponse.toString());
          }
        })
        .toCompletable();
    Completable cpWrite = Completable.defer(() -> {
      beforeCommand(baseCommand);
      RxBus.getInstance().post(baseCommand);
      return writeCharacteristic(getCharacteristic(baseCommand.getWriteCharacteristicUUID()), false,
          baseCommand.getBytes());
    });
    Completable cpRead = readCharacteristic(GattUUID.Characteristic.Response.getUuid());

    return mQueueTaskExecutor.queue(cpResponse.mergeWith(cpWrite.concatWith(cpRead))
        .doOnComplete(() -> RxBus.getInstance().post(new CommandCompleteEvent(baseCommand)))
        .doOnError(
            throwable -> RxBus.getInstance().post(new CommandErrorEvent(baseCommand, throwable)))
        .timeout(TIMEOUT_COMMAND, TimeUnit.MILLISECONDS));
  }

  private void beforeCommand(BaseCommand baseCommand) {
    mMeasureSpeed.reset();
  }

  @Override protected void onCharacteristicChanged(UUID uuid, byte[] data, int length) {
    super.onCharacteristicChanged(uuid, data, length);
    mMeasureSpeed.receive(data.length);
  }

  @Override protected void onConnectionStateChanged(StateInfo.ConnectionState connectionState) {
    super.onConnectionStateChanged(connectionState);
    mMeasureSpeed.reset();
  }

  @Override protected Completable beforeDisconnect() {
    return Completable.defer(() -> {
      if (!getStateInfo().isConnected()) {
        return Completable.complete();
      }
      return setNotificationEnabled(getCharacteristic(GattUUID.Characteristic.RawData.getUuid()),
          false).doOnComplete(() -> Timber.i("disable indication success"))
          .doOnError(throwable -> Timber.w(throwable, throwable.getMessage()))
          .onErrorComplete();
    });
  }

  @Override protected void onConnectedCommunication() {
    Timber.i("onConnectedCommunication");
    //add by herman for test
    /*sendCommand(new AskTemperatureCommand()).subscribe(() -> Timber.i("AskTemperatureCommand..."),
            throwable -> Timber.e(throwable, "AskTemperatureCommand..."));*/
    //end

    sendCommand(new SetTimeoutCommand(0xFF)).subscribe(() -> Timber.i("set timeout=255"),
        throwable -> Timber.e(throwable, "SetTimeoutCommand"));
    requestConnectionPriority(GattConnectPriority.CONNECTION_PRIORITY_HIGH).subscribe(
        () -> Timber.i("onConnectedCommunication success"),
        throwable -> Timber.w(throwable, "requestConnectionPriority"));
    setNotificationEnabled(getCharacteristic(GattUUID.Characteristic.RawData.getUuid()),
        true).subscribe(() -> Timber.i("enable indication success"),
        throwable -> Timber.w(throwable, "setNotificationEnabled"));
  }

  public long getBps() {
    return mMeasureSpeed.bps();
  }

  public Completable readSystemInfo() {
    return readCharacteristic(GattUUID.Characteristic.SysInfo.getUuid());
  }

  private static class MT6381DataReceiver implements BLEDataReceiver {

    private final MT6381SystemInfoParser mSystemInfoParser = new MT6381SystemInfoParser();
    private final MT6381RawDataParser mt6381RawDataParser = new MT6381RawDataParser();
    private final MT6381RawDataParserForTemp rawDataParserForTemp = new MT6381RawDataParserForTemp();

    @Override public void reset() {
      mSystemInfoParser.reset();
      mt6381RawDataParser.reset();
    }

    //todo by herman for temp.
    @Override public void receive(UUID uuid, byte[] data, int length) {
      if (uuid.equals(GattUUID.Characteristic.RawData.getUuid())) {
        //add by herman for temp.
        //One UUID for temperature and ECG
        Log.d("MT6381Peripheral","data.length: " + data.length);
        //Negative number +256 and then converted to hex
        //Log.d("MT6381Peripheral","data[0]: " + data[0]);//-86 to hex :-86 + 256 =170=0xAA // -79  to hex: -79 + 256 =177=0XB1
        //Log.d("MT6381Peripheral","data[1]: " + data[1]);//85 to hex: 0x55   // 10 to hex:  0x0A
        if(data.length <= 2){
          rawDataParserForTemp.receive(uuid, data, length);
          //end
        }else{
          mt6381RawDataParser.receive(uuid, data, length);
        }
      } else if (uuid.equals(GattUUID.Characteristic.Response.getUuid()) || uuid.equals(
          GattUUID.Characteristic.SysInfo.getUuid())) {
        mSystemInfoParser.receive(uuid, data, length);
      }
    }

    @Override public void destroy() {
      mt6381RawDataParser.destroy();
    }
  }
}
