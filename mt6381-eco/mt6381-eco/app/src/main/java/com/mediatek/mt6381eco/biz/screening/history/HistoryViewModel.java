package com.mediatek.mt6381eco.biz.screening.history;

import android.arch.lifecycle.MutableLiveData;
import com.mediatek.mt6381eco.dagger.FragmentScoped;
import com.mediatek.mt6381eco.viewmodel.Resource;
import java.util.ArrayList;
import javax.inject.Inject;

@FragmentScoped
public class HistoryViewModel {
  public final MutableLiveData<Resource<ArrayList<HistoryViewItem>>> dataList = new MutableLiveData<>();
  public boolean hasMore = true;
  @Inject HistoryViewModel(){

  }
}
