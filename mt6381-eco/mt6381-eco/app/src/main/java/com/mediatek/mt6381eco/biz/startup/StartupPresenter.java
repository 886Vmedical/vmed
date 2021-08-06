package com.mediatek.mt6381eco.biz.startup;

import android.util.Log;

import com.mediatek.mt6381eco.db.AppDatabase;
import com.mediatek.mt6381eco.db.EasyDao;
import com.mediatek.mt6381eco.db.entries.Profile;
import com.mediatek.mt6381eco.network.OAuthHelper;
import com.mediatek.mt6381eco.utils.MTextUtils;
import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;
import javax.inject.Inject;

public class StartupPresenter implements StartupContract.Presenter {
  private final StartupContract.View mView;
  private final OAuthHelper mAuthHelper;
  private final AppDatabase mAppDataBase;
  private final EasyDao mEasyDao;

  @Inject StartupPresenter(StartupContract.View view, AppDatabase appDatabase,EasyDao easyDao,
      OAuthHelper authHelper) {
    mView = view;
    mAppDataBase = appDatabase;
    mAuthHelper = authHelper;
    mEasyDao = easyDao;
    if (authHelper.isTokenValid()) {
      navToNext();
    }
  }

  @Override public void navToNext(){
    Profile profile = mAppDataBase.profileDao().findProfile();
    //modify by herman 从访客模式切换到用户模式，ID无效的问题, 以及测量时无效的问题,前提是登录后需要跳转到profile界面
    if(profile == null || MTextUtils.isEmpty(profile.getNickName()) || profile.getProfileId().equals("88888")){
      mView.requireProfile();
    }else {
      mView.navToHome();
    }
    //mView.requireProfile();
  }

  @Override public void destroy() {


  }

  @Override public Completable requestGuest() {
    Completable cmpGuest = mAuthHelper.guest();
    return cmpGuest.subscribeOn(Schedulers.io()).doOnComplete(mView::requireProfile);
  }

//add by herman for guest
  public boolean requestGuestForSB(boolean isguest) {
    mAuthHelper.guestForSB(isguest);
    mView.requireProfile();
    return true;
  }
//end

  @Override public void profileInvalid() {
    mAuthHelper.logout();
  }
}
