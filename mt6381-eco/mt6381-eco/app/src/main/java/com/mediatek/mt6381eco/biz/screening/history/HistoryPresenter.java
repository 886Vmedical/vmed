package com.mediatek.mt6381eco.biz.screening.history;

import com.mediatek.mt6381eco.db.AppDatabase;
import com.mediatek.mt6381eco.network.ApiService;
import com.mediatek.mt6381eco.viewmodel.Resource;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import javax.inject.Inject;

public class HistoryPresenter implements HistoryContract.Presenter {

  private final ApiService mApiService;
  private final String mProfileId;
  private final HistoryViewModel mViewModel;
  private final CompositeDisposable mDisposables = new CompositeDisposable();
  private final ArrayList<HistoryViewItem> mCachedList = new ArrayList<>();

  @Inject HistoryPresenter(ApiService apiService, AppDatabase database,
      HistoryViewModel viewModel) {
    mApiService = apiService;
    mProfileId = database.profileDao().findProfile().getProfileId();
    mViewModel = viewModel;
  }

  @Override public void destroy() {
    mDisposables.clear();
  }


  @Override public void loadHistory(int limit, int offset) {
    mViewModel.dataList.postValue(Resource.loading(new ArrayList<>(mCachedList)));
    mDisposables.add(mApiService.retrieveScreening(mProfileId, limit, offset).map(resp -> {
      HistoryViewItem[] ret = new HistoryViewItem[resp.data.length];
      for (int i = 0; i < ret.length; ++i) {
        HistoryViewItem item = new HistoryViewItem();
        item.riskLevel = resp.data[i].risklevel;
        item.riskProbability = resp.data[i].riskprobability;
        item.timestamp = new Date(resp.data[i].timestamp);
        item.measurementId = resp.data[i].measurementId;
        item.profileId = mProfileId;
        ret[i] = item;
      }
      return ret;
    }).subscribeOn(Schedulers.io()).subscribe(historyViewItems -> {
      mCachedList.addAll(Arrays.asList(historyViewItems));
      mViewModel.dataList.postValue(Resource.success(new ArrayList<>(mCachedList)));
    }, throwable -> mViewModel.dataList.postValue(
        Resource.error(throwable, new ArrayList<>(mCachedList)))));
  }
}
