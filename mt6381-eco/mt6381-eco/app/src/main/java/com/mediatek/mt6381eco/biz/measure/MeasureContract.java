package com.mediatek.mt6381eco.biz.measure;

import com.mediatek.mt6381eco.biz.peripheral.IPeripheral;
import com.mediatek.mt6381eco.mvp.BasePresenter2;
import com.mediatek.mt6381eco.mvp.BaseView;
import java.io.Serializable;

public interface MeasureContract {
  interface Presenter extends BasePresenter2 {

    void attach(IPeripheral peripheral);

    void startMeasure(boolean downSample);

    void pauseReplay();

    void startReplay();

    PresenterState getSaveState();

    void restoreSaveState(PresenterState saveState);

    void abort();

    void toggleEKGChecking();

    void togglePPG1Checking();
    void togglePPG2Checking();
  }

  interface View extends BaseView {
  }

  class PresenterState {
    public static final String KEY_STATE_NAME = "STATE_NAME";
    public static final String KEY_TRANS_OBJECT = "transObject";

    public String stateName;
    public Serializable transObject;
  }
}
