package com.mediatek.mt6381eco.biz.recorddetail;

import android.support.v4.util.Pair;
import com.mediatek.mt6381eco.biz.measure.view.DatatypeConverter;
import com.mediatek.mt6381eco.biz.measure.view.ECGFilterService;
import com.mediatek.mt6381eco.biz.measure.view.PPGFilterService;
import com.mediatek.mt6381eco.biz.utlis.BizUtils;
import com.mediatek.mt6381eco.db.AppDatabase;
import com.mediatek.mt6381eco.db.entries.Profile;
import com.mediatek.mt6381eco.network.ApiService;
import com.mediatek.mt6381eco.viewmodel.Resource;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import javax.inject.Inject;
import okhttp3.ResponseBody;

public class RecordDetailPresenter implements RecordDetailContract.Presenter {

  protected final Profile mProfile;
  private final RecordDetailViewModel mViewModel;
  private final ApiService mApiService;
  private final CompositeDisposable mDisposables = new CompositeDisposable();

  @Inject RecordDetailPresenter(RecordDetailViewModel viewModel, ApiService apiService,
      AppDatabase appDatabase) {
    mViewModel = viewModel;
    mApiService = apiService;
    mProfile = appDatabase.profileDao().findProfile();
  }

  @Override public void destroy() {
    mDisposables.clear();
  }

  @Override public void loadWaveformData(String profileId, int measurementId) {
    mDisposables.add(mApiService.getSingleMeasurementData(profileId, measurementId)
        .map(responseBody -> {
          File zipFile = BizUtils.saveZipFile(responseBody.byteStream());
          File rawFile = BizUtils.gunzipFile(zipFile);
          zipFile.delete();
          BufferedReader reader =
              new BufferedReader(new InputStreamReader(new FileInputStream(rawFile)));
          final String EKG_TYPE = "5";
          final String PPG_TYPE = "9";
          ECGFilterService ecgFilterService = new ECGFilterService();
          PPGFilterService ppgFilterService = new PPGFilterService();
          ArrayList<Float> ecgs = new ArrayList<>();
          ArrayList<Float> ppgs = new ArrayList<>();
          String line;
          float sumMv = 0f;
          while ((line = reader.readLine()) != null) {
            if (line.startsWith(EKG_TYPE)) {
              String[] ss = line.split(",");
              for (int i = 0; i < 12; ++i) {
                int intVal = Integer.valueOf(ss[i + 2]);
                float mv = DatatypeConverter.ecgConvertToMv(intVal);
                sumMv += mv;
                if (i % 4 == 3) {
                  float avgMv = sumMv / 4;
                  sumMv = 0f;
                  float drawMv = ecgFilterService.filter(avgMv);
                  ecgs.add(drawMv);
                }
              }
              sumMv = 0f;
            } else if (line.startsWith(PPG_TYPE)) {
              String[] ss = line.split(",");
              for (int i = 0; i < 12; ++i) {
                int intVal = Integer.valueOf(ss[i + 2]);
                float mv = DatatypeConverter.ppg1ConvertToMv(intVal);
                sumMv += mv;
                if (i % 4 == 3) {
                  float avgMv = sumMv / 4;
                  sumMv = 0f;
                  float drawMv = ppgFilterService.filter(avgMv) * 0.01f * -1;
                  ppgs.add(drawMv);
                }
              }
              sumMv = 0f;
            }
          }
          reader.close();
          rawFile.delete();
          return new Pair<>(ecgs, ppgs);
        })
        .toObservable()
        .subscribeOn(Schedulers.io())
        .doOnSubscribe(disposable -> mViewModel.rawData.postValue(Resource.loading(null)))
        .subscribe(pair -> mViewModel.rawData.postValue(Resource.success(pair)),
            throwable -> mViewModel.rawData.postValue(Resource.error(throwable, null))));
  }

  @Override public void loadMetaData(String profileId, int measurementId) {
    mViewModel.meta.postValue(Resource.loading(null));
    mDisposables.add(mApiService.getSingleMeasurement(profileId, measurementId)
        .subscribeOn(Schedulers.io())
        .subscribe(measurement -> mViewModel.meta.postValue(Resource.success(measurement)),
            throwable -> mViewModel.meta.postValue(Resource.error(throwable, null))));
  }
}
