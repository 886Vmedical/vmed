package com.mediatek.mt6381eco.biz.viewmodel;

import android.arch.lifecycle.MutableLiveData;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton public class AppViewModel {
  public final MutableLiveData<Boolean> needRelogin = new MutableLiveData<>();
  public final MutableLiveData<Account> account = new MutableLiveData<>();

  @Inject
  public AppViewModel() {
    needRelogin.setValue(false);
  }

  public static class Account {
    public boolean isGuest;
    public Permission permission;
  }
  public static class Permission {
    public final boolean screening;

    public Permission(boolean screening) {
      this.screening = screening;
    }
  }
}
