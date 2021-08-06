package com.mediatek.mt6381eco.biz.measure;

public class DataLostEvent {

  public static final int DATA_TYPE_EKG = 1;
  public static final int DATA_TYPE_PPG1 = 2;
  public static final int DATA_TYPE_PPG2 = 3;
  public int type;

  public DataLostEvent(int type) {
    this.type = type;
  }
}
