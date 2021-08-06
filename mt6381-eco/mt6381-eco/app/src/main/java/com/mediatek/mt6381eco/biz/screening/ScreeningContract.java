package com.mediatek.mt6381eco.biz.screening;

import com.mediatek.mt6381eco.mvp.BasePresenter2;
import com.mediatek.mt6381eco.mvp.BaseView;
import io.reactivex.Completable;

public interface ScreeningContract {

  interface View extends BaseView {

  }

  interface Presenter extends BasePresenter2<View> {

  }
}
