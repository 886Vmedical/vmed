package com.mediatek.mt6381eco.biz.profile;

import com.mediatek.mt6381eco.mvp.BasePresenter2;
import com.mediatek.mt6381eco.mvp.BaseView;
import io.reactivex.Completable;

public interface ProfileContract {

  interface View extends BaseView {

    void navToNext();
  }

  interface Presenter extends BasePresenter2<View> {

    Completable requestSaveProfile();
    void requestSaveProfileForSB();
  }
}
