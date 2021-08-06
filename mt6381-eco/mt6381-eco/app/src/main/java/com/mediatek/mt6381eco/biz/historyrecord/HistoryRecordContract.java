package com.mediatek.mt6381eco.biz.historyrecord;

import com.mediatek.mt6381eco.mvp.BasePresenter2;
import com.mediatek.mt6381eco.mvp.BaseView;
import io.reactivex.Single;

public interface HistoryRecordContract {

  interface View extends BaseView {

  }

  interface Presenter extends BasePresenter2<View> {

    Single<HistoryRecordViewItem[]> requestLoadRecords(int limit, int offset);
  }
}
