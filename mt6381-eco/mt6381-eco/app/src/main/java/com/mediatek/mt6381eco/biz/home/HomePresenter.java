package com.mediatek.mt6381eco.biz.home;

import com.mediatek.mt6381eco.biz.peripheral.IPeripheral;
import com.mediatek.mt6381eco.db.AppDatabase;
import com.mediatek.mt6381eco.db.ProfileDao;
import com.mediatek.mt6381eco.db.entries.Profile;
import com.mediatek.mt6381eco.network.ApiService;
import com.mediatek.mt6381eco.network.OAuthHelper;
import com.mediatek.mt6381eco.utils.MappingUtils;
import com.mediatek.mt6381eco.viewmodel.Resource;
import io.reactivex.Completable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import javax.inject.Inject;

public class HomePresenter implements HomeContract.Presenter {
  private final HomeContract.View mView;
  private final OAuthHelper mAuthHelper;
  private final ProfileDao mProfileDao;
  private final ApiService mApiService;
  private final HomeViewModel mViewModel;
  private IPeripheral mPeripheral;
  private final CompositeDisposable mDisposables = new CompositeDisposable();

  @Inject HomePresenter(HomeContract.View view, OAuthHelper authHelper, AppDatabase appDatabase,
      ApiService apiService, HomeViewModel homeViewModel) {
    mView = view;
    mAuthHelper = authHelper;
    mProfileDao = appDatabase.profileDao();
    mApiService = apiService;
    mViewModel = homeViewModel;
  }

  @Override public void destroy() {
    mDisposables.clear();
  }

  @Override public void disconnect() {
    mPeripheral.disconnect();
  }

  @Override public void attach(IPeripheral peripheral) {
    mPeripheral = peripheral;
  }

  @Override public Completable requestSignOut() {
    return Completable.create(e -> {
      mAuthHelper.logout();
      Profile profile = mProfileDao.findProfile();
      if (profile != null) {
        mProfileDao.deleteProfile(profile);
      }
      e.onComplete();
    }).doOnComplete(mView::navToStartup);
  }

  @Override public void downgrade() {
    mView.startLoading();
    mDisposables.add(mAuthHelper.downgrade()
        .subscribeOn(Schedulers.io())
        .subscribe(mView::stopLoading, throwable -> {
          mView.stopLoading();
          mView.showError(throwable);
        }));
  }

  @Override public void upgrade() {
    mView.startLoading();
    mDisposables.add(mAuthHelper.upgrade()
        .subscribeOn(Schedulers.io())
        .subscribe(mView::stopLoading, throwable -> {
          mView.stopLoading();
          mView.showError(throwable);
        }));
  }

  @Override public void deleteCalibration() {
    Profile profile = mProfileDao.findProfile();
    if (profile != null) {
      Completable cmpProfile = mApiService.getProfiles().doOnSuccess(profileListResponse -> {
        if (profileListResponse.data.length > 0) {
          mProfileDao.insertProfile(MappingUtils.toDbEntry(profileListResponse.data[0]));
        }
      }).toCompletable();

      mDisposables.add(mApiService.deleteCalibration(profile.getProfileId())
          .concatWith(cmpProfile)
          .subscribeOn(Schedulers.io())
          .doOnSubscribe(
              disposable -> mViewModel.deleteCalibrationResource.postValue(Resource.loading(null)))
          .subscribe(() -> mViewModel.deleteCalibrationResource.postValue(Resource.success(null)),
              throwable -> mViewModel.deleteCalibrationResource.postValue(
                  Resource.error(throwable, null))));
    }
  }
}
