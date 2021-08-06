package com.mediatek.mt6381eco.biz.screening.history;

import com.mediatek.mt6381eco.mvp.BasePresenter2;
import com.mediatek.mt6381eco.mvp.BaseView;
import io.reactivex.Single;

public interface HistoryContract {
  interface View extends BaseView{

  }
  interface Presenter extends BasePresenter2<View> {
    void loadHistory(int limit, int offSet);
  }
}
