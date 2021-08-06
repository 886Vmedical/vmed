package com.mediatek.mt6381eco.biz.recorddetail;

import android.arch.lifecycle.MutableLiveData;
import android.support.v4.util.Pair;
import com.mediatek.mt6381eco.dagger.FragmentScoped;
import com.mediatek.mt6381eco.network.model.Measurement;
import com.mediatek.mt6381eco.viewmodel.Resource;
import java.util.ArrayList;
import javax.inject.Inject;

@FragmentScoped
public class RecordDetailViewModel {

  public MutableLiveData<Resource<Measurement>> meta = new MutableLiveData<>();
  public MutableLiveData<Resource<Pair<ArrayList<Float>, ArrayList<Float>>>> rawData = new MutableLiveData<>();

  @Inject RecordDetailViewModel(){

  }
}
