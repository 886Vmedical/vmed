package com.mediatek.mt6381eco.biz.calibration;

import android.util.Log;

import com.mediatek.jni.mt6381.Utils;
import com.mediatek.mt6381eco.biz.measure.BaseMeasurePresenter;
import com.mediatek.mt6381eco.biz.measure.BaseMeasureViewModel;
import com.mediatek.mt6381eco.biz.measure.MeasureContract;
import com.mediatek.mt6381eco.dagger.SupportSensorTypes;
import com.mediatek.mt6381eco.db.AppDatabase;
import com.mediatek.mt6381eco.db.ProfileDao;
import com.mediatek.mt6381eco.network.model.CalibrationObject;
import com.mediatek.mt6381eco.network.ApiService;
import com.mediatek.mt6381eco.utils.MappingUtils;
import com.mediatek.mt6381eco.viewmodel.Resource;
import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.Arrays;
import javax.inject.Inject;

public class CalibrationPresenter extends BaseMeasurePresenter
    implements CalibrationContract.Presenter {

  private final CalibrationViewModel mCalibrationViewModel;
  private final ProfileDao mProfileDao;
  private int[] mCalibrationData = new int[18];
  private static final int TARGET_COUNT = 512 * 25;
  private int mCalibrationStep = 0;
  @Inject CalibrationPresenter(BaseMeasureViewModel viewModel,CalibrationViewModel calibrationViewModel, ApiService apiService,
      AppDatabase appDatabase, SupportSensorTypes supportSensorTypes) {
    super(viewModel, apiService, appDatabase, supportSensorTypes);
    setTargetDataCount(TARGET_COUNT);
    mCalibrationViewModel = calibrationViewModel;
    mProfileDao = appDatabase.profileDao();
  }

  @Override protected void onCompleted() {
    super.onCompleted();
    int[] soData = Utils.bpAlgGetCalibrationData(6);
    System.arraycopy(soData, 0, mCalibrationData, mCalibrationStep * 6 + 6, 6);
    ++ mCalibrationStep;
    mCalibrationViewModel.goldenInput.postValue(mCalibrationStep);

  }

  @Override public void uploadCalibration() {
    Log.d("CalibrationPresenter","uploadCalibration");
    Completable cmpProfile = mApiService.getProfiles().doOnSuccess(profileListResponse -> {
      if (profileListResponse.data.length > 0) {
        mProfileDao.insertProfile(MappingUtils.toDbEntry(profileListResponse.data[0]));
      }
    }).toCompletable();
    
    CalibrationObject request = new CalibrationObject();
    request.setData(toList(mCalibrationData));
    mDisposables.add(mApiService.createCalibrations2(mProfile.getProfileId(), request).concatWith(cmpProfile)
        .subscribeOn(Schedulers.io())
        .doOnSubscribe(disposable -> mCalibrationViewModel.uploadResource.postValue(Resource.loading(null)))
        .subscribe(() -> mCalibrationViewModel.uploadResource.postValue(Resource.success(null)),
            throwable -> mCalibrationViewModel.uploadResource.postValue(Resource.error(throwable, null))));
  }

  private ArrayList<Integer> toList(int[] data) {
    ArrayList<Integer> ret = new ArrayList<>();
    for (int value : data) {
      ret.add(value);
    }
    return ret;
  }


  @Override public void reset() {
    mCalibrationStep = 0;
    Arrays.fill(mCalibrationData, 0);
  }

  @Override public void inputGolden(int step, int sbp, int dbp, int hr) {
    mCalibrationData[step * 2] = sbp;
    mCalibrationData[step * 2 + 1] = dbp;
  }


  @Override public MeasureContract.PresenterState getSaveState() {
    MeasureContract.PresenterState superSaveState = super.getSaveState();
    CalibrationContract.PresenterState ret = new CalibrationContract.PresenterState();
    ret.stateName = superSaveState.stateName;
    ret.transObject = superSaveState.transObject;
    ret.calibrationData = mCalibrationData;
    return ret;
  }

  @Override public void restoreSaveState(MeasureContract.PresenterState saveState) {
    super.restoreSaveState(saveState);
    mCalibrationData = (( CalibrationContract.PresenterState)saveState).calibrationData;
  }
}
