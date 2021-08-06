package com.mediatek.mt6381eco.biz.connect;

import android.arch.lifecycle.MutableLiveData;
import com.mediatek.mt6381eco.dagger.FragmentScoped;
import com.mediatek.mt6381eco.viewmodel.Resource;
import javax.inject.Inject;

@FragmentScoped
public class ConnectViewModel {
  public final MutableLiveData<Resource> connection = new MutableLiveData<>();
  public final MutableLiveData<Integer> throughputWarning = new MutableLiveData<>();

  @Inject ConnectViewModel(){

  }
}
