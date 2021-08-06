package com.mediatek.mt6381eco.biz.temp;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mediatek.mt6381eco.biz.viewmodel.AppViewModel;
import com.mediatek.mt6381eco.db.AppDatabase;
import com.mediatek.mt6381eco.db.ProfileDao;
import com.mediatek.mt6381eco.db.entries.Profile;
import com.mediatek.mt6381eco.network.ApiService;
import com.mediatek.mt6381eco.network.model.MeasurementResponse;
import com.mediatek.mt6381eco.network.model.TempRequest;
import com.mediatek.mt6381eco.network.model.TempResponse;
import com.mediatek.mt6381eco.utils.MappingUtils;
import com.mediatek.mt6381eco.viewmodel.Resource;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import timber.log.Timber;


public class TempPresenter implements TempContract.Presenter {

    private final TempContract.View mView;
    private final AppViewModel mAppViewModel;
    private final ProfileDao mProfileDao;
    private final ApiService mApiService;

    private final CompositeDisposable mDisposables = new CompositeDisposable();


    @Inject
    TempPresenter(TempContract.View view, AppViewModel appViewModel, AppDatabase appDatabase, ApiService apiService) {

        mView = view;
        mAppViewModel = appViewModel;
        mProfileDao = appDatabase.profileDao();
        mApiService = apiService;
    }


    @Override
    public void destroy() {

    }

    @Override
    public void onUploadtemp() {
        Log.d("TemperatureActivity", "onUploadtemp: " + "onUploadtemp...");
        Profile profile = mProfileDao.findProfile();

        boolean isGuest =
                mAppViewModel.account.getValue() != null && mAppViewModel.account.getValue().isGuest;
        Log.d("TemperatureActivity", "isGuest: " + isGuest);
        Log.d("TemperatureActivity", "mAppViewModel.account.getValue()" + mAppViewModel.account.getValue());
        Log.d("TemperatureActivity", "mApiService: " + mApiService);
        Log.d("TemperatureActivity", "profile.getProfileId(): " + profile.getProfileId());

        if (!isGuest) {
            TempRequest mTempRequest = new TempRequest();
            mTempRequest.temperature = mView.getOneUploadTemp();
            mTempRequest.timestamp = System.currentTimeMillis();

            Gson gson = new GsonBuilder()
                    .serializeNulls()
                    .excludeFieldsWithoutExposeAnnotation()
                    .create();
            RequestBody requestBody =
                    RequestBody.create(MediaType.parse("application/json"), gson.toJson(mTempRequest));

            //add by herman 不转化为Disposable 无法上传,根据返回值调整
            Single<TempResponse> mApiTempResponse;
            //返回值为Single<TempResponse>
            mApiTempResponse = mApiService.createTemp(profile.getProfileId(), mTempRequest);

            //Single to Completable to Disposable
            mDisposables.add(mApiTempResponse.doOnSuccess(tempResponse -> {
                Log.d("TemperatureActivity", "temperatureId:"+ String.valueOf(tempResponse.temperatureId)); })
                    .subscribeOn(Schedulers.io())
                    .toCompletable()
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe(disposable -> Resource.loading(null))
                    .subscribe(() -> Resource.success(null),
                            throwable -> Resource.error(throwable, null)));


            //返回值为Completable
            //Completable to Disposable
             /*mApiService.createTemp2(profile.getProfileId(), mTempRequest)
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe(disposable -> Resource.loading(null))
                    .subscribe(() -> Resource.success(null),
                            throwable -> Resource.error(throwable,null));*/

            /*mDisposables.add(mApiService.createTemp2(profile.getProfileId(), mTempRequest)
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe(disposable -> Resource.loading(null))
                    .subscribe(() -> Resource.success(null),
                                throwable -> Resource.error(throwable,null)));*/

            Log.d("TemperatureActivity", "onUploadtemp: " + "onUploadtemp success");
        } else {
            // do nothing 或者这里保存数据到本地。

        }
    }

}
