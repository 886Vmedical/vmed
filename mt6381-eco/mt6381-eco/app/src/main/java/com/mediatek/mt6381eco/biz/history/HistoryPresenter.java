package com.mediatek.mt6381eco.biz.history;

import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Pair;
import com.mediatek.mt6381eco.db.AppDatabase;
import com.mediatek.mt6381eco.network.ApiService;
import com.mediatek.mt6381eco.network.model.MeasureRetrieveResponse;
import com.mediatek.mt6381eco.utils.MTimeUtils;
import com.mediatek.mt6381eco.viewmodel.Resource;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.Calendar;
import javax.inject.Inject;

public class HistoryPresenter implements HistoryContract.Presenter {
  protected static final String COLUMNS_HR_SPO2 = "heartRate,spo2";
  protected static final String COLUMNS_HRV = "fatigue,pressure";
  protected static final String COLUMNS_BP = "systolic,diastolic";
  protected static final String COLUMNS_BRV = "brv";
  protected static final String COLUMNS_TEMPERATURE = "temperature";
  protected static final String SPAN_DAY = "day";
  protected static final String SPAN_WEEK = "week";
  protected static final String SPAN_MONTH = "month";
  private final ApiService mApiService;
  private final HistoryViewModel mViewModel;
  private final String mProfileId;
  private final CompositeDisposable mDisposables = new CompositeDisposable();

  @Inject HistoryPresenter(ApiService apiService, AppDatabase appDatabase,
      HistoryViewModel viewModel) {
    mApiService = apiService;
    mViewModel = viewModel;
    mProfileId = appDatabase.profileDao().findProfile().getProfileId();
  }

  @Override public void setView(HistoryContract.View view) {
  }

  @Override public void destroy() {
    mDisposables.clear();
  }

  @Override public void requestRetrieveMeasurements(String span, String columns) {
    Calendar calendar = Calendar.getInstance();
    String endTime = MTimeUtils.formatTimeInGMT(calendar.getTimeInMillis());
    switch (span) {
      case SPAN_DAY: {
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        break;
      }
      case SPAN_WEEK: {
        calendar.add(Calendar.WEEK_OF_YEAR, -1);
        break;
      }
      case SPAN_MONTH: {
        calendar.add(Calendar.MONTH, -1);
        break;
      }
    }
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    String startTime = MTimeUtils.formatTimeInGMT(calendar.getTimeInMillis());
    HistoryViewModel.Result result = new HistoryViewModel.Result();

    Log.d("tempListData Presenter","columns: " + columns);
    if(columns.equals(COLUMNS_TEMPERATURE)){
      //查詢溫度
      //single to disposable
      mDisposables.add(mApiService.retrievetemps(mProfileId, startTime, endTime)
              .subscribeOn(Schedulers.io())
              .map(response -> {
                //HistoryViewModel.Result result = new HistoryViewModel.Result();
                result.tempListData = response.data;
                Log.d("tempListData Presenter","result.tempListData: " + result.tempListData);
                if (!result.tempListData.isEmpty()) {
                  //result.xMin = result.tempListData.get(result.tempListData.size() - 1).getTimestamp();
                  //result.xMax = result.tempListData.get(0).getTimestamp();
                  Log.d("tempListData Presenter","result.tempListData.size: " + result.tempListData.size());
                }
                return result;
              })
              .doOnSubscribe(disposable -> mViewModel.result.postValue(Resource.loading(null)))
              .subscribe(result2 -> mViewModel.result.postValue(Resource.success(result2)),
                      throwable -> mViewModel.result.postValue(Resource.error(throwable, null))));
    }else{
      mDisposables.add(mApiService.retrieveMeasurements(mProfileId, columns, startTime, endTime)
              .subscribeOn(Schedulers.io())
              .map(response -> {
                result.yTopHighLow = getHighLow(response.monthlyStats.fatigue);
                result.yBottomHighLow = getHighLow(response.monthlyStats.pressure);
                result.listData = response.data;
                Log.d("tempListData Presenter","result.listData: " + result.listData);
                if (!result.listData.isEmpty()) {
                  //result.xMin = result.listData.get(result.listData.size() - 1).getTimestamp();
                  //result.xMax = result.listData.get(0).getTimestamp();
                  Log.d("tempListData Presenter","result.listData.size: " + result.listData.size());
                }
                return result;
              })
              .doOnSubscribe(disposable -> mViewModel.result.postValue(Resource.loading(null)))
              .subscribe(result1 -> mViewModel.result.postValue(Resource.success(result1)),
                      throwable -> mViewModel.result.postValue(Resource.error(throwable, null))));
    }
  }

  @NonNull private Pair<Float, Float> getHighLow(
      MeasureRetrieveResponse.MonthlyStatsBean.MonthlyStats monthlyStats) {
    if (monthlyStats != null) {
      return new Pair<>(monthlyStats.getHighVal(), monthlyStats.getLowVal());
    } else {
      return new Pair<>(0f, 0f);
    }
  }
}
