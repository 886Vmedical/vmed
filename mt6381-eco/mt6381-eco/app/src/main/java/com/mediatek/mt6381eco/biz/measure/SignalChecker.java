package com.mediatek.mt6381eco.biz.measure;

import timber.log.Timber;

public class SignalChecker {
  public static final int SIGNAL_FINGER_EKG =    0b00000001;
  public static final int SIGNAL_FINGER_PPG1 =   0b00000010;

  public static final int SIGNAL_QUALITY_EKG =   0b00000100;
  public static final int SIGNAL_QUALITY_PPG1 =  0b00001000;
  public static final int SIGNAL_QUALITY_PPG2 =  0b00010000;
  public static final int SIGNAL_EKG = SIGNAL_FINGER_EKG | SIGNAL_QUALITY_EKG;
  public static final int SIGNAL_PPG1 = SIGNAL_FINGER_PPG1 | SIGNAL_QUALITY_PPG1;
  public static final int SIGNAL_PPG2 =  SIGNAL_QUALITY_PPG2;
  public static final int SIGNAL_STATUS_GOOD = SIGNAL_EKG | SIGNAL_PPG1 | SIGNAL_PPG2;
  public static final int DEFAULT_CHECKING = SIGNAL_EKG | SIGNAL_PPG1;
  private final Callback mCallback;
  private static int mCheckingFlag = DEFAULT_CHECKING;
  private int status = SIGNAL_STATUS_GOOD;

  public SignalChecker(Callback callback){
    mCallback = callback;
    reset();
  }


  public void reset(){
    status = SIGNAL_STATUS_GOOD;
  }

  public int getStatus() {
    return status;
  }


  public boolean isGoodSignal(int status){
    return isGoodSignal(status, SIGNAL_STATUS_GOOD);
  }

  public boolean isGoodSignal(int status, int type){
    return (status & type & mCheckingFlag) == (type & mCheckingFlag);
  }

  public void toggleChecking(int type){
    mCheckingFlag ^= type;
    mCallback.onStatusChange(status);
  }

  public boolean isChecking(int type){
    return (mCheckingFlag & type) == type;
  }

  public void setStatus(int type, boolean isGood){
    int temp = status;
    temp &= ~type;
    if (isGood) {
      temp |= type;
    }
    if (status != temp) {
      mCallback.onStatusChange(temp);
      Timber.d("signal:%d", temp);
    }
    status = temp;
  }

  public int getCheckingFlag(){
    return mCheckingFlag;
  }

  public interface Callback{
    void onStatusChange(int status);
  }
}
