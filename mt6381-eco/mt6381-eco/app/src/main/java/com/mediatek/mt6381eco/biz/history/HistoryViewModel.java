package com.mediatek.mt6381eco.biz.history;

import android.arch.lifecycle.MutableLiveData;
import android.util.Pair;
import com.mediatek.mt6381eco.dagger.ActivityScoped;
import com.mediatek.mt6381eco.network.model.MeasureResult;
import com.mediatek.mt6381eco.network.model.TemperatureResult;
import com.mediatek.mt6381eco.viewmodel.Resource;
import java.util.List;
import javax.inject.Inject;

@ActivityScoped public class HistoryViewModel {
  public final MutableLiveData<Resource<Result>> result = new MutableLiveData<>();

  @Inject HistoryViewModel() {
  }

  public static class Result {
    public Long xMin;
    public Long xMax;
    public Pair yTopHighLow;
    public Pair yBottomHighLow;
    public List<MeasureResult> listData;
    public List<TemperatureResult> tempListData;
  }
}
