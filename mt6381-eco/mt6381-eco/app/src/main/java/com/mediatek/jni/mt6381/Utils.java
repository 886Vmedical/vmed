package com.mediatek.jni.mt6381;

public class Utils {

  // testSoLib
  static int idx = 0;

  static {
    System.loadLibrary("Bsp");
  }

  // bp
  public static native void bpAlgInit();

  // checkQuality
  public static native void checkQualityInit(int ecg_enable);

  // spo2
  public static native void spo2Init();

  public static native void bpAlgSetUserInfo(int age, int gender, int height, int weight,
      int arm_length); // 35,1,175,70,70

  public static native void bpAlgSetCalibrationData(int[] data_in, int data_length);

  public static native int checkQuality(int[] data_ppg1, int data_ppg1_len, int[] data_ppg2,
      int data_ppg2_len, int[] data_ecg, int data_ecg_len, int status, int reserved);

  public static native int[] bpAlgGetCalibrationData(int data_length);

  public static native int bpAlgGetStatus();

  public static native int hrvAlgGetStatus();

  public static native int bpAlgGetSbp();

  public static native int bpAlgGetDbp();

  public static native int bpAlgGetBpm();

  public static native int bpAlgGetSDNN();

  public static native int bpAlgGetLF();

  public static native int bpAlgGetHF();

  public static native float bpAlgGetLFHF();

  public static native int bpAlgGetFatigueIndex();

  public static native int bpAlgGetPressureIndex();

  public static native int spo2GetBpm();

  public static native int spo2GetSpO2();

  public static native int ohrmGetVersion();

  public static native int spo2GetVersion();

  public static native int bpAlgGetVersion();

  public static native int HRVGetVersion();

  //Take Medicine
  public static native int bpAlgSetPersonalStatus(int status); // low 0; middium 1; high 2;

  public static native int bpAlgSetTakeMedicineTime(int time); // 24 hours, 0 ~ 23

  public static native int bpAlgSetCurrentTime(int time); // 24 hours, 0 ~ 23

  public static native int bpAlgSetTakeMedicineType(int medicineType);

  // export ECG R peak position. get R peak counts (buffer size)
  //public static native int hrvGetRpeakLength();

  // export ECG R peak position. get R peak buffer (unit: sample)
  //public static native int[] hrvGetRpeakPos();

  // export ECG R peak position. get R peak counts (buffer size)
  public static native int hrvGetRpeakIntervalCount();

  // export ECG R peak position. get RRI buffer (unit: sample)
  public static native int[] hrvGetRpeakInterval();

  // export ECG RRI spectrum. get spectrum buffer size (currently fixed at 41)
  public static native int hrvGetSpectrumLength();

  // export ECG RRI spectrum. get spectrum buffer (spacing 0.01Hz. unit: ms^2/Hz)
  public static native int[] hrvGetSpectrum();  





}
