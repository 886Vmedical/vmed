package com.mediatek.mt6381eco.biz.historyrecord;

import android.util.Log;

import com.mediatek.mt6381eco.db.AppDatabase;
import com.mediatek.mt6381eco.network.model.MeasureResult;
import com.mediatek.mt6381eco.network.ApiService;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;

public class HistoryRecordPresenter implements HistoryRecordContract.Presenter {
  private final ApiService mApiService;
  private final String mProfileId;
  private final CompositeDisposable mDisposables = new CompositeDisposable();

  @Inject HistoryRecordPresenter(ApiService apiService, AppDatabase appDatabase) {
    mApiService = apiService;
    mProfileId = appDatabase.profileDao().findProfile().getProfileId();
  }

  @Override public Single<HistoryRecordViewItem[]> requestLoadRecords(int limit, int offset) {

    return mApiService.retrieveMeasurements2(mProfileId, limit, offset).map(resp -> {
      List<MeasureResult> listData = resp.data;
      HistoryRecordViewItem[] ret = new HistoryRecordViewItem[listData.size()];
      for (int i = 0; i < listData.size(); ++i) {
        HistoryRecordViewItem item = new HistoryRecordViewItem();
        item.timestamp = new Date(listData.get(i).getTimestamp());
        item.measurementId = listData.get(i).getMeasurementId();
        item.profileId = mProfileId;
        Log.d("HistoryRecordPresenter","item.measurementId: " + item.measurementId );
        Log.d("HistoryRecordPresenter","item.timestamp: " + item.timestamp );
        Log.d("HistoryRecordPresenter","item.profileId: " + item.profileId );

        ret[i] = item;
      }
      return ret;
    }).subscribeOn(Schedulers.io());
  }

  @Override public void destroy() {
    mDisposables.clear();
  }
}
