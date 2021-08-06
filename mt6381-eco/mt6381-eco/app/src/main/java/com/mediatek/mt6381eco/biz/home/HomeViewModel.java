package com.mediatek.mt6381eco.biz.home;

import android.arch.lifecycle.MutableLiveData;
import com.mediatek.mt6381eco.dagger.ActivityScoped;
import com.mediatek.mt6381eco.viewmodel.Resource;
import javax.inject.Inject;
import lombok.Getter;

@ActivityScoped
@Getter
public class HomeViewModel {
  private final MutableLiveData<Integer> connectionState = new MutableLiveData<>();
  public final MutableLiveData<Resource> deleteCalibrationResource = new MutableLiveData<>();
  @Inject HomeViewModel(){

  }
}
