package com.mediatek.mt6381eco.biz.history;

import com.mediatek.mt6381eco.mvp.BasePresenter;
import com.mediatek.mt6381eco.mvp.BaseView;

public interface HistoryContract {
  interface View extends BaseView {
    void setEmptyChart();

    void stopLoading();
  }

  interface Presenter extends BasePresenter<View> {

    void requestRetrieveMeasurements(String span, String columns);
  }
}
