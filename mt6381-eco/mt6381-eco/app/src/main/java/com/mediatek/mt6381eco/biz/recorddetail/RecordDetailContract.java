package com.mediatek.mt6381eco.biz.recorddetail;

import com.mediatek.mt6381eco.mvp.BasePresenter2;
import com.mediatek.mt6381eco.mvp.BaseView;

public interface RecordDetailContract {
  interface View extends BaseView{

  }
  interface Presenter extends BasePresenter2<View> {

    void loadWaveformData(String profileId, int measurementId);

    void loadMetaData(String profileId, int measurementId);
  }
}
