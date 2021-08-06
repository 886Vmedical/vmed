package com.mediatek.mt6381eco.biz.measure;

import android.util.Log;

import com.mediatek.mt6381eco.biz.utlis.BizUtils;
import com.mediatek.mt6381eco.dagger.SupportSensorTypes;
import com.mediatek.mt6381eco.db.AppDatabase;
import com.mediatek.mt6381eco.network.ApiService;
import com.mediatek.mt6381eco.network.model.MeasurementRequest;
import com.mediatek.mt6381eco.viewmodel.Resource;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
import javax.inject.Inject;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class MeasurePresenter extends BaseMeasurePresenter {
  private final MeasureViewModel mMeasureViewModel;
  private int mMeasurementId;

  @Inject
  protected MeasurePresenter(BaseMeasureViewModel viewModel, MeasureViewModel completedViewModel,
      ApiService apiService, AppDatabase appDatabase, SupportSensorTypes supportSensorTypes) {
    super(viewModel, apiService, appDatabase, supportSensorTypes);
    mMeasureViewModel = completedViewModel;
  }

  @Override protected void onCompleted() {
    super.onCompleted();
    //这里需要判断是否为访客模式
      //add by herman for guest no network
      boolean isGuest =
              mAppViewModel.account.getValue() != null && mAppViewModel.account.getValue().isGuest;
      Log.d("MeasurePresenter","isGuest: " + isGuest);
    if(!isGuest){
        uploadMeasurement();
    }else{
        uploadMeasurementForGuest();
    }
  }

  //add by herman for guest
   private void uploadMeasurementForGuest(){
      //获取请求的测量数据
       MeasurementRequest request = new MeasurementRequest();
       request.systolic = mAlgMeasureResult.sbp;
       request.diastolic = mAlgMeasureResult.dbp;

       request.fatigue = mAlgMeasureResult.fatigue;
       request.pressure = mAlgMeasureResult.pressure;
       request.heartRate = mAlgMeasureResult.bpm;
       request.spo2 = mAlgMeasureResult.spo2;
       //modify by herman
       request.timestamp = System.currentTimeMillis();

       //将请求数据转为显示的结果数据
       MeasureViewModel.Result result = new MeasureViewModel.Result();

       result.heartRate = request.heartRate;
       result.spo2 = request.spo2;
       result.sbp = request.systolic;
       result.dbp = request.diastolic;
       result.fatigue = request.fatigue;
       result.pressure = request.pressure;

       result.riskLevel = 0;
       result.riskProbability = 0;
       result.confidenceLevel = 0;

       //mMeasurementId = 888;
       //mMeasureViewModel.result.postValue(Resource.loading(null));
       mMeasureViewModel.result.postValue(Resource.success(result));

   }

   //end add by herman for guest

  private void uploadMeasurement() {
    MeasurementRequest request = new MeasurementRequest();
    request.systolic = mAlgMeasureResult.sbp;
    request.diastolic = mAlgMeasureResult.dbp;

    request.fatigue = mAlgMeasureResult.fatigue;
    request.pressure = mAlgMeasureResult.pressure;
    request.heartRate = mAlgMeasureResult.bpm;
    request.spo2 = mAlgMeasureResult.spo2;
    //modify by herman
    request.timestamp = System.currentTimeMillis();

    MeasureViewModel.Result result = new MeasureViewModel.Result();

    mDisposables.add(
        mApiService.createMeasurements2(mProfile.getProfileId(), request)
            .doOnSuccess(response -> {
              result.heartRate = request.heartRate;
              result.spo2 = request.spo2;
              result.sbp = request.systolic;
              result.dbp = request.diastolic;
              result.fatigue = request.fatigue;
              result.pressure = request.pressure;
              mMeasurementId = response.measurementId;
            })
            .flatMap(response -> {
                File file = BizUtils.gzipFile(mNormalLogger.getCurrentFile());
              RequestBody requestFile =
                  RequestBody.create(MediaType.parse("application/gzip"), file);
              //modify by herman toUpCase: rawData
              MultipartBody.Part body =
                  MultipartBody.Part.createFormData("rawData", file.getName(), requestFile);
              return mApiService.uploadRawData(mProfile.getProfileId(), mMeasurementId, body)
                  .doOnSuccess(uploadRawDataResponse -> file.delete())
                  .doOnError(throwable -> file.delete());
            })
            .doOnSuccess(uploadRawDataResponse -> {
              result.riskLevel = uploadRawDataResponse.riskLevel;
              result.riskProbability = uploadRawDataResponse.riskProbability;
              result.confidenceLevel = uploadRawDataResponse.confidenceLevel;
            })
            .toCompletable()
            .subscribeOn(Schedulers.io())
            .doOnSubscribe(disposable -> mMeasureViewModel.result.postValue(Resource.loading(null)))
            .subscribe(() -> mMeasureViewModel.result.postValue(Resource.success(result)),
                throwable -> mMeasureViewModel.result.postValue(Resource.error(throwable, null))));
  }
}