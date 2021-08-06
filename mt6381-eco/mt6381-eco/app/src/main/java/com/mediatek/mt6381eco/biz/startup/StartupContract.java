package com.mediatek.mt6381eco.biz.startup;

import com.mediatek.mt6381eco.mvp.BasePresenter2;
import com.mediatek.mt6381eco.mvp.BaseView;
import io.reactivex.Completable;

public interface StartupContract {
  interface View extends BaseView {

    void navToHome();

    void requireProfile();


  }

  interface Presenter extends BasePresenter2<View> {
    Completable requestGuest();
    //add by herman for guest
    boolean requestGuestForSB(boolean b);

    void profileInvalid();

    void navToNext();

  }
}
