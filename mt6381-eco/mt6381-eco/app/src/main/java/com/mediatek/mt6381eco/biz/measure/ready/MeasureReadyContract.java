package com.mediatek.mt6381eco.biz.measure.ready;

import com.mediatek.mt6381eco.biz.peripheral.IPeripheral;
import com.mediatek.mt6381eco.mvp.BasePresenter;
import com.mediatek.mt6381eco.mvp.BaseView;

public interface MeasureReadyContract {

  interface View extends BaseView {
    void navMeasurePage(boolean downSample);

    void navHomePage();

    void setContentView();

    void alterThroughput(long throughput, int allowMinThroughput);

    void exit();
  }

  interface Presenter extends BasePresenter<View> {

    void attach(IPeripheral peripheral);

    void stopMeasure();
  }
}
