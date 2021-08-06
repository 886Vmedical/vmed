package com.mediatek.mt6381eco.biz.account.signin;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.mediatek.mt6381eco.biz.account.AccountContract;
import com.mediatek.mt6381eco.biz.account.AccountViewModel;
import com.mediatek.mt6381eco.biz.account.BaseAccountFragment;
import com.mediatek.mt6381eco.db.AppDatabase;
import com.mediatek.mt6381eco.db.ProfileDao;
import com.mediatek.mt6381eco.network.ApiService;
import com.mediatek.mt6381eco.network.OAuthHelper;
import com.mediatek.mt6381eco.network.model.LoginRequest;
import com.mediatek.mt6381eco.utils.MappingUtils;
import com.mediatek.mt6381eco.viewmodel.Resource;
import io.reactivex.Completable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import javax.inject.Inject;

import static android.os.ParcelFileDescriptor.MODE_APPEND;

public class SignInPresenter implements AccountContract.Presenter {

  private final ApiService mApiService;
  private final OAuthHelper mAuthHelper;
  private final ProfileDao mProfileAdo;
  private final AccountViewModel mViewModel;
  private final CompositeDisposable mDisposables = new CompositeDisposable();

  @Inject SignInPresenter(AccountViewModel viewMode, ApiService apiService, AppDatabase appDatabase,
      OAuthHelper authHelper) {
    mViewModel = viewMode;
    mApiService = apiService;
    mProfileAdo = appDatabase.profileDao();
    mAuthHelper = authHelper;
  }

  @Override public void destroy() {
    mDisposables.clear();
  }

  @Override public void requestAccount(String account, String password) {
    LoginRequest loginRequest = new LoginRequest();
    loginRequest.username = account;
    loginRequest.password = password;
    Completable cmpProfile = mApiService.getProfiles().doOnSuccess(profileListResponse -> {
      if (profileListResponse.data.length > 0) {
        mProfileAdo.insertProfile(MappingUtils.toDbEntry(profileListResponse.data[0]));
      }
    }).toCompletable();


    Completable cmpLogin = mAuthHelper.login(loginRequest);
    mDisposables.add(cmpLogin.concatWith(cmpProfile)
        .doOnSubscribe(disposable -> mViewModel.loginRequest.postValue(Resource.loading(null)))
        .subscribeOn(Schedulers.io())
        .subscribe(() -> mViewModel.loginRequest.postValue(Resource.success(null)),
            throwable -> mViewModel.loginRequest.postValue(Resource.error(throwable, null))));

  }
}
