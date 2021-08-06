package com.mediatek.mt6381eco.biz.calibration;

import android.util.Log;

import com.mediatek.mt6381eco.biz.measure.MeasureContract;
import com.mediatek.mt6381eco.biz.peripheral.IPeripheral;
import com.mediatek.mt6381eco.mvp.BaseView;

public interface CalibrationContract {

  interface Presenter extends MeasureContract.Presenter{


    void reset();

    void inputGolden(int step, int sbp, int dbp, int hr);

    void uploadCalibration();

  }

  interface  View extends BaseView{
  }

  class PresenterState extends MeasureContract.PresenterState{
    public static final String KEY_CALIBRATION ="clibration";
    public int[] calibrationData;
  }
}
