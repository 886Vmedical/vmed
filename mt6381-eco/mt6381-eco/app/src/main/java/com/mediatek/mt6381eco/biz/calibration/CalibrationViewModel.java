package com.mediatek.mt6381eco.biz.calibration;

import android.arch.lifecycle.MutableLiveData;
import com.mediatek.mt6381eco.dagger.FragmentScoped;
import com.mediatek.mt6381eco.viewmodel.Resource;
import javax.inject.Inject;

@FragmentScoped
public class CalibrationViewModel {
  public final MutableLiveData<Resource> uploadResource = new MutableLiveData<>();
  public final MutableLiveData<Integer> goldenInput = new MutableLiveData<>();

  @Inject CalibrationViewModel(){

  }
}
