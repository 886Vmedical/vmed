package com.mediatek.mt6381.ble;

import android.util.SparseArray;
import com.mediatek.blenativewrapper.BLEDataReceiver;
import com.mediatek.blenativewrapper.rxbus.RxBus;
import com.mediatek.mt6381.ble.command.MeasurementCommand;
import com.mediatek.mt6381eco.biz.peripheral.SensorData;
import com.mediatek.mtk6381.data_parsing.DataParsing;
import io.reactivex.disposables.CompositeDisposable;
import java.util.Arrays;
import java.util.UUID;
import timber.log.Timber;

public class MT6381RawDataParser implements BLEDataReceiver {
  private static final int DUMMY = 12345;
  private static final int DUMMY_COUNT = 11;
  private final CompositeDisposable mDisposables = new CompositeDisposable();
  private final SparseArray<SensorDataSNHandler> mSNHandlerMap = new SparseArray<>();
  private final SensorData[] sensorDataBuffer = new SensorData[(384 + 240) * 2];
  private static final String SENSOR_CLS_NAME = SensorData.class.getName().replaceAll("\\.", "/");

  public MT6381RawDataParser() {
    mDisposables.add(RxBus.getInstance()
        .toFlowable(MeasurementCommand.class)
        .subscribe(measurementCommand -> reset()));
    mDisposables.add(
        RxBus.getInstance().toFlowable(MeasurementCommand.class).subscribe(this::setDataType));
    initSensorDataBuffer();
    reset();
  }

  private void initSensorDataBuffer() {
    for (int i = 0; i < sensorDataBuffer.length; ++i) {
      sensorDataBuffer[i] = new SensorData();
    }
  }

  private void setDataType(MeasurementCommand measurementCommand) {

  }

  private int[] getDataType(int flag) {
    int[] dataTypes = new int[3];
    int index = 0;
    if ((flag & MeasurementCommand.EKG_FLAG) > 0) {
      dataTypes[index++] = SensorData.DATA_TYPE_EKG;
    }
    if ((flag & MeasurementCommand.PPG1_FLAG) > 0) {
      dataTypes[index++] = SensorData.DATA_TYPE_PPG1;
    }
    if ((flag & MeasurementCommand.PPG2_FLAG) > 0) {
      dataTypes[index++] = SensorData.DATA_TYPE_PPG2;
    }

    if ((flag & MeasurementCommand.EKG_THROUGHPUT_FLAG) > 0) {
      dataTypes[index++] = SensorData.DATA_TYPE_EKG;
    }
    return Arrays.copyOf(dataTypes, index);
  }

  @Override public void reset() {
    Timber.d("data_parsing_init");
    DataParsing.data_parsing_init();
    mSNHandlerMap.clear();
  }

  @Override public void receive(UUID uuid, byte[] data, int length) {
    int count = DataParsing.data_parsing(sensorDataBuffer, SENSOR_CLS_NAME, data, length);
    Timber.d("data_parsing.count = %d", count);
    for (int i = 0; i < count; ++i) {
      SensorDataSNHandler handler = mSNHandlerMap.get(sensorDataBuffer[i].type);
      if (handler == null) {
        handler = new SensorDataSNHandler();
        mSNHandlerMap.put(sensorDataBuffer[i].type, handler);
      }
      SensorData sensorData = handler.handle(sensorDataBuffer[i]);
      com.mediatek.mt6381eco.rxbus.RxBus.getInstance().post(sensorData);
      if (sensorData.type == SensorData.DATA_TYPE_LED_SETTING) {
        for (int j = 0; j < DUMMY_COUNT; ++j) {
          sensorData.value = DUMMY;
          com.mediatek.mt6381eco.rxbus.RxBus.getInstance().post(sensorData);
        }
      }
    }
  }

  @Override public void destroy() {
    mDisposables.clear();
  }
}
