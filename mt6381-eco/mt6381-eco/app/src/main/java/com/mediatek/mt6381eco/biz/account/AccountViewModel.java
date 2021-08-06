package com.mediatek.mt6381eco.biz.account;

import android.arch.lifecycle.MutableLiveData;
import com.mediatek.mt6381eco.dagger.FragmentScoped;
import com.mediatek.mt6381eco.viewmodel.Resource;
import javax.inject.Inject;

@FragmentScoped
public class AccountViewModel {

  public final MutableLiveData<Resource> loginRequest = new MutableLiveData<>();
  @Inject AccountViewModel(){

  }
}
