package com.mediatek.mt6381eco.biz.measure;

import android.arch.lifecycle.MutableLiveData;
import com.mediatek.mt6381eco.dagger.FragmentScoped;
import com.mediatek.mt6381eco.viewmodel.Resource;
import javax.inject.Inject;

@FragmentScoped
public class MeasureViewModel {

  public final MutableLiveData<Resource<Result>> result = new MutableLiveData<>();
  @Inject MeasureViewModel(){

  }
  public static class Result{
    public int heartRate;
    public int spo2;
    public int sbp;
    public int dbp;
    public int fatigue;
    public int pressure;
    public int confidenceLevel;
    public int riskLevel;
    public int riskProbability;
  }
}
