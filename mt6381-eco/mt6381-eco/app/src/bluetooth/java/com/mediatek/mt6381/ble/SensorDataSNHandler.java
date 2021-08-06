package com.mediatek.mt6381.ble;

import com.mediatek.mt6381eco.biz.peripheral.SensorData;
import timber.log.Timber;

public class SensorDataSNHandler {
  private static final int ROTATE = 256;
  private int mLastRealSn = -1;
  private int mLastSN = -1;

  public void reset() {
    mLastRealSn = -1;
    mLastSN = -1;
  }

  public SensorData handle(SensorData sensorData) {
    int delta = sensorData.sn - mLastRealSn;
    mLastRealSn = sensorData.sn;
    if (delta < 0) {
      delta += ROTATE;
    }
    if (delta != 1) {
      Timber.w("data_lost delta:%d : %d - %d", delta, sensorData.type, sensorData.sn);
    }
    sensorData.sn = mLastSN + delta;
    mLastSN = sensorData.sn;
    SensorData ret = sensorData;
/*    ret= new SensorData();
    sensorData.copyTo(ret)*/
      return ret;
  }
}
