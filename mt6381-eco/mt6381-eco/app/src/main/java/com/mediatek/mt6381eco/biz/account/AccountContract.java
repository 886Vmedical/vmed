package com.mediatek.mt6381eco.biz.account;

import com.mediatek.mt6381eco.mvp.BasePresenter2;
import com.mediatek.mt6381eco.mvp.BaseView;
import io.reactivex.Completable;

public interface AccountContract {

  interface View extends BaseView {

    void navToNext();
  }

  interface Presenter extends BasePresenter2<View> {

    void requestAccount(String account, String password);
  }
}
