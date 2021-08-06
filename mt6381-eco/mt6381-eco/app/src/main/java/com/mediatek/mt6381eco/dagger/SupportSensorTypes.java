package com.mediatek.mt6381eco.dagger;

import com.mediatek.mt6381eco.biz.peripheral.SensorData;

public abstract class SupportSensorTypes {
  public static final String STRING_TYPE_EKG = "EKG";
  public static final String STRING_TYPE_PPG1 = "PPG1";
  public static final String STRING_TYPE_PPG2 = "PPG2";
  protected static final int[] DATA_TYPE_FULL = new int[] {
      SensorData.DATA_TYPE_EKG, SensorData.DATA_TYPE_PPG1, SensorData.DATA_TYPE_PPG2
  };

  public abstract int[] getTypeIntArray();

  public String[] getTypeStringArray() {
    int[] intTypes = getTypeIntArray();
    String[] ret = new String[intTypes.length];
    for (int i = 0; i < intTypes.length; ++i) {
      ret[i] = typeToString(intTypes[i]);
    }
    return ret;
  }

  private String typeToString(final int type) {
    switch (type) {
      case SensorData.DATA_TYPE_EKG:
        return STRING_TYPE_EKG;
      case SensorData.DATA_TYPE_PPG1:
        return STRING_TYPE_PPG1;
      case SensorData.DATA_TYPE_PPG2:
        return STRING_TYPE_PPG2;
    }
    throw new RuntimeException(type + " is not a excepted sensor type value");
  }
}
