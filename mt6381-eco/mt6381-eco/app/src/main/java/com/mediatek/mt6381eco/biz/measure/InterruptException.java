package com.mediatek.mt6381eco.biz.measure;


public class InterruptException extends Exception {
  public static final int TYPE_CONNECTION_LOST = 0;
  public static final int TYPE_FINER_OFF = 1;
  public static final int TYPE_START_MEASURE_FAIL = 2;
  public static final int TYPE_ERROR = 3;
  public static final int TYPE_STATE_LOST = 4;
  public static final int TYPE_ABORT = 5;


  public final int type;
  public final Throwable error;

  public InterruptException(int type) {
    super();
    this.type = type;
    error = null;
  }
  public InterruptException(Throwable throwable){
    super();
    error = throwable;
    type = TYPE_ERROR;
  }


}
