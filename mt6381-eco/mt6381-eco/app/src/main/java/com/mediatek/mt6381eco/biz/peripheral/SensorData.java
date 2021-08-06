package com.mediatek.mt6381eco.biz.peripheral;

import timber.log.Timber;

public class SensorData {
  public static final int DATA_TYPE_EKG = 1;
  public static final int DATA_TYPE_PPG1 = 2;
  public static final int DATA_TYPE_PPG2 = 3;
  public static final int DATA_TYPE_LED_SETTING = 4;
  public static final int DATA_TYPE_AMB1 = 5;

  public int type;
  public int sn;
  public int status;
  public int value;

  public SensorData() {
  }

  public SensorData(int type, int sn) {
    this.type = type;
    this.sn = sn;
  }

  public void copyTo(SensorData other) {
    other.type = type;
    other.sn = sn;
    other.status = status;
    other.value = value;
  }

  public void printString() {
    Timber.d("type:%s,sn:%s,status:%s,value:%s", this.type, this.sn, this.status, this.value);
  }
}
