package com.mediatek.mt6381eco.biz.dataReceiver;

import android.util.SparseArray;
import android.util.SparseIntArray;
import com.mediatek.jni.mt6381.Utils;
import com.mediatek.mt6381eco.BuildConfig;
import com.mediatek.mt6381eco.biz.measure.IntArrayVersion;
import com.mediatek.mt6381eco.biz.peripheral.SensorData;
import com.mediatek.mt6381eco.db.entries.Profile;
import com.mediatek.mt6381eco.log.MFileLogger;
import com.mediatek.mt6381eco.utils.DataConverter;
import com.mediatek.mt6381eco.utils.MTimeUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import timber.log.Timber;

import static com.mediatek.mt6381eco.db.entries.Profile.GENDER_MALE;

public class RawDataLogger {
  public static final int RAW_TYPE_EKG = 5;
  public static final int RAW_TYPE_PPG1 = 9;
  public static final int RAW_TYPE_PPG2 = 10;
  public static final int RAW_TYPE_AMB = 8101;
  public static final int RAW_TYPE_LED_SETTING = 8102;
  public static final int RAW_TYPE_PPG1_FINGER_STATUS = 8103;
  public static final int RAW_TYPE_EKG_FINGER_STATUS = 8303;
  public static final int RAW_TYPE_HRSPO2_RESULT = 80;
  public static final int RAW_TYPE_BLOOD_PRESSURE_RESULT = 81;
  public static final int RAW_TYPE_HRV_RESULT = 82;

  private static final int DATA_LENGTH = 12;
  private static final int PACKAGE_LENGTH = DATA_LENGTH + 2;
  private static final int DUMMY = 12345;
  boolean isReady = false;
  private final MFileLogger mFileLogger;
  private final SparseArray<ArrayList<Integer>> mSensorDataMap = new SparseArray<>();
  private final SparseIntArray mOriginSnMap = new SparseIntArray();
  private final SparseIntArray mSnMap = new SparseIntArray();
  private Profile mProfile;
  private int[] mCalibrationArray;
  private final SparseArray<IntArrayVersion> mIntArrayVersionMap = new SparseArray<>();
  private boolean mIsDownSample;

  public RawDataLogger(String fileNamePattern) {
    mFileLogger = new MFileLogger(fileNamePattern);
  }

  public static int toSensorType(int rawType) {
    switch (rawType) {
      case RAW_TYPE_EKG:
        return SensorData.DATA_TYPE_EKG;
      case RAW_TYPE_PPG1:
        return SensorData.DATA_TYPE_PPG1;
      case RAW_TYPE_PPG2:
        return SensorData.DATA_TYPE_PPG2;
      case RAW_TYPE_AMB:{
        return SensorData.DATA_TYPE_AMB1;
      }
      case RAW_TYPE_LED_SETTING:{
        return SensorData.DATA_TYPE_LED_SETTING;
      }
    }
    return rawType;
  }

  public void setHeaderObject(Profile profile, int[] calibrationArray, boolean isDownsample) {
    mProfile = profile;
    mCalibrationArray = calibrationArray;
    mIsDownSample = isDownsample;
  }

  public synchronized void start() {
    isReady = true;
    mFileLogger.reset();
    String[] headers = getHeaders();
    for (String line : headers) {
      mFileLogger.write(line);
      mFileLogger.newLine();
    }
    mIntArrayVersionMap.clear();
    snStartFromZero();
  }

  public synchronized void startIfNeed(){
    if(!isReady){
      start();
    }
  }

  public synchronized void snStartFromZero() {
    mSnMap.clear();
    mOriginSnMap.clear();
  }

  public synchronized void receiveData(int type, int sn, int value) {
    if (isReady) {
      ArrayList<Integer> arrayList = mSensorDataMap.get(type);
      int tmpSn = getCurrentSn(type, sn);
      if (arrayList == null) {
        arrayList = new ArrayList<>(PACKAGE_LENGTH);
        mSensorDataMap.put(type, arrayList);
      }
      if (arrayList.size() == 0) {
        arrayList.add(mapRawDataType(type));
        arrayList.add(tmpSn);
      }

      arrayList.add(value);
      if (arrayList.size() == PACKAGE_LENGTH) {
        for (int tmpValue : arrayList) {
          mFileLogger.write(String.valueOf(tmpValue));
          mFileLogger.write(',');
        }
        mFileLogger.write(String.valueOf(System.currentTimeMillis()));
        mFileLogger.newLine();
        arrayList.clear();
      }
    }
  }

  public synchronized void receiveResult(int type, Object... values){
    if (isReady) {
      IntArrayVersion intArrayVersion = mIntArrayVersionMap.get(type, new IntArrayVersion());
      if(intArrayVersion.isChanged(values)){
        Timber.d("receiveResult: %d,%s", type,Arrays.toString(values));
        mFileLogger.write(String.valueOf(type));
        mFileLogger.write(',');
        int i = 0;
        for(; i < values.length; ++i){
          mFileLogger.write(values[i].toString());
          mFileLogger.write(',');
        }
        for(;i < DATA_LENGTH + 1;++i){
          mFileLogger.write(String.valueOf(DUMMY));
          mFileLogger.write(',');
        }
        mFileLogger.write(String.valueOf(System.currentTimeMillis()));
        mFileLogger.newLine();
      }

      mIntArrayVersionMap.put(type, intArrayVersion);

    }
  }

  private Integer mapRawDataType(int type) {
    switch (type) {
      case SensorData.DATA_TYPE_EKG:
        return RAW_TYPE_EKG;
      case SensorData.DATA_TYPE_PPG1:
        return RAW_TYPE_PPG1;
      case SensorData.DATA_TYPE_PPG2:
        return RAW_TYPE_PPG2;
      case SensorData.DATA_TYPE_AMB1:
        return RAW_TYPE_AMB;
      case SensorData.DATA_TYPE_LED_SETTING:
        return RAW_TYPE_LED_SETTING;
    }
    return type;
  }

  private int getCurrentSn(int type, int sn) {
    int lastSn = mSnMap.get(type, -1);
    int lastOriginSn = mOriginSnMap.get(type, -1);
    int tmpSn = 0;
    if (lastSn > -1) {
      int delta = sn - lastOriginSn;
      tmpSn = delta + lastSn;
    }
    mSnMap.put(type, tmpSn);
    mOriginSnMap.put(type, sn);
    return tmpSn;
  }

  public synchronized void stop() {
    isReady = false;
    mFileLogger.close();
    mSensorDataMap.clear();
    mIntArrayVersionMap.clear();
  }

  public File getCurrentFile() {
    return mFileLogger.getCurrentFile();
  }

  public String[] getHeaders() {
    ArrayList<String> headers = new ArrayList<>();
    headers.add(String.format(Locale.getDefault(), "format version: 0.3,v%s,%d,%d,%d,%d",
        BuildConfig.VERSION_NAME, Utils.ohrmGetVersion(), Utils.spo2GetVersion(),
        Utils.bpAlgGetVersion(), Utils.HRVGetVersion()));

    int age = MTimeUtils.calcAge(new Date(mProfile.getBirthday()));
    String gender = mProfile.getGender() == GENDER_MALE ? "Male" : "Female";
    int height = mProfile.getHeight();
    //int height = DataConverter.calcHeight(mProfile.getHeight(), mProfile.getHeightUnit());
    int weight = DataConverter.calcWeight(mProfile.getWeight(), mProfile.getWeightUnit());
    String takeMedicineTime = mProfile.getTakeMedicineTime() == null ? "12345"
        : Integer.toString(mProfile.getTakeMedicineTime());
    String line = String.format(Locale.getDefault(),
        "1000,0,%s,%s,%d,%d,%d,%d,%s,%d,12345,12345,12345,12345,%d", mProfile.getUniqueId(),
        gender, age, height, weight, mProfile.getPersonalStatus(), takeMedicineTime,
        mIsDownSample ? 1:0,
        System.currentTimeMillis());
    headers.add(line);
    
    if (mCalibrationArray != null) {
      for(int i = 0;i < mCalibrationArray.length ; i += 12){
        int[] temp = new int[12];
        Arrays.fill(temp, DUMMY);
        System.arraycopy(mCalibrationArray, i, temp, 0, Math.min(12, mCalibrationArray.length -i));
        line = String.format(Locale.getDefault(), "1010,%d,%s,%d",i,
            DataConverter.intArrayToString(temp,0), System.currentTimeMillis());
        headers.add(line);
      }
    }
    String[] ret = new String[headers.size()];
    ret = headers.toArray(ret);
    return ret;
  }

  public void delete() {
    if (getCurrentFile() == null || !getCurrentFile().delete()) {
      Timber.w("can not delete logger");
    }
  }
}
