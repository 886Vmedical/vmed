package com.mediatek.mt6381eco.network;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import com.mediatek.mt6381eco.biz.viewmodel.AppViewModel;
import com.mediatek.mt6381eco.db.EasyDao;
import com.mediatek.mt6381eco.db.entries.AuthInfo;
import com.mediatek.mt6381eco.network.model.AuthResponse;
import com.mediatek.mt6381eco.network.model.LoginRequest;
import com.mediatek.mt6381eco.utils.MTextUtils;
import io.reactivex.Completable;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import okhttp3.Authenticator;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

import static android.os.ParcelFileDescriptor.MODE_APPEND;

@Singleton public class OAuthHelper implements Authenticator, Interceptor {

  private static final int AUTHORITY_MAX_RETRY = 1;
  //modify by herman
  private final static String BEARER_FORMATTER = "%s";
  //private final static String BEARER_FORMATTER = "bear %s";
  private final OAuthService mOAuthService;
  private final EasyDao mEasyDao;
  private final AppViewModel mAppViewModel;
  private String mBearerToken = "";
  private AuthInfo mAuthData;

  @Inject
  public OAuthHelper(OAuthService oAuthService, EasyDao easyDao, AppViewModel appViewModel) {
    mEasyDao = easyDao;
    mOAuthService = oAuthService;
    mAppViewModel = appViewModel;
    set(mEasyDao.find(AuthInfo.class));
  }

  private void set(AuthResponse authResponse, boolean isGuest) {
    AuthInfo authInfo = new AuthInfo();
    authInfo.accessToken = authResponse.accessToken;
    authInfo.refreshToken = authResponse.refreshToken;
    authInfo.screeningsPermission = authResponse.screeningsPermission;
    authInfo.isValid = true;
    authInfo.isGuest = isGuest;
    set(authInfo);
  }

  private synchronized void set(AuthInfo authInfo) {
    if (authInfo == null) {
      authInfo = new AuthInfo();
      authInfo.isValid = false;
    }
    mAuthData = authInfo;
    mBearerToken = String.format(BEARER_FORMATTER, mAuthData.accessToken);
    mEasyDao.save(mAuthData);
    AppViewModel.Account account = new AppViewModel.Account();
    account.permission = new AppViewModel.Permission(mAuthData.screeningsPermission);
    account.isGuest = mAuthData.isGuest;
    mAppViewModel.account.postValue(account);
    if (mAppViewModel.needRelogin.getValue() != !isTokenValid()) {
      mAppViewModel.needRelogin.postValue(!isTokenValid());
    }
  }

  public Completable login(LoginRequest request) {
    return mOAuthService.login(request)
        .doOnSuccess(response -> set(response, false))
        .toCompletable();
  }

  private Completable refreshToken() {
    String refreshToken = mAuthData.refreshToken;
/*    if (BuildConfig.BUILD_TYPE.equals("debug")) {
      refreshToken = "invalid_refresh_token_for_debug";
    }*/
    return mOAuthService.refreshToken(String.format(BEARER_FORMATTER, refreshToken))
        .doOnSuccess(response -> set(response, mAuthData.isGuest))
        .doOnError(throwable -> {
          if (throwable instanceof RetrofitException) {
            RetrofitException exception = (RetrofitException) throwable;
            if (exception.getKind() == RetrofitException.Kind.HTTP
                && exception.getResponse().code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
              mAuthData.isValid = false;
              set(mAuthData);
            }
          }
        })
        .toCompletable();
  }

  public Completable guest() {
    return mOAuthService.guest().doOnSuccess(response -> set(response, true)).toCompletable();
  }

  //add by herman for guest
  public boolean guestForSB(boolean isguest) {
    if (mAuthData == null) {
      mAuthData = new AuthInfo();
      mAuthData.isValid = false;
    }
    mAuthData.isGuest = isguest;
    mBearerToken = String.format(BEARER_FORMATTER, mAuthData.accessToken);
    mEasyDao.save(mAuthData);
    AppViewModel.Account account = new AppViewModel.Account();
    account.permission = new AppViewModel.Permission(mAuthData.screeningsPermission);
    account.isGuest = isguest;
    mAppViewModel.account.postValue(account);
    if (mAppViewModel.needRelogin.getValue() != !isTokenValid()) {
      mAppViewModel.needRelogin.postValue(!isTokenValid());
    }
    return true;
  }
  //end


  public Completable downgrade() {
    return mOAuthService.downgrade(String.format(BEARER_FORMATTER, mAuthData.refreshToken))
        .doOnSuccess(response -> set(response, mAuthData.isGuest))
        .toCompletable();
  }

  public Completable upgrade() {
    return mOAuthService.upgrade(String.format(BEARER_FORMATTER, mAuthData.refreshToken))
        .doOnSuccess(response -> set(response, mAuthData.isGuest))
        .toCompletable();
  }

  public Completable register(LoginRequest request) {
    HashMap<String, String> headers = new HashMap<>();
    if (mAuthData != null) {
      headers.put(OAuthService.HEADER_AUTHORIZATION,
          String.format(BEARER_FORMATTER, mAuthData.refreshToken));
    }
    return mOAuthService.register(request, headers).doOnSuccess(response -> set(response, false)).toCompletable();
  }

  public boolean isTokenValid() {
    return !MTextUtils.isEmpty(mAuthData.refreshToken) && mAuthData.isValid;
  }

  @Nullable @Override public Request authenticate(@NonNull Route route, @NonNull Response response)
      throws IOException {
    if (responseCount(response) + 1 > AUTHORITY_MAX_RETRY) {
      return null; // If we've failed 3 times, give up. - in real life, never give up!!
    }
    synchronized (this) {
      if (mBearerToken.equals(response.request().header(OAuthService.HEADER_AUTHORIZATION))) {
        try {
          refreshToken().blockingAwait();
          return response.request()
              .newBuilder()
              .header(OAuthService.HEADER_AUTHORIZATION, mBearerToken)
              .build();
        } catch (Exception e) {
          e.printStackTrace();
          return null;
        }
      }
    }
    return null;
  }

  @Override public Response intercept(@NonNull Chain chain) throws IOException {
    Request original = chain.request();
    String oAuthheader = original.header(OAuthService.HEADER_AUTHORIZATION);
    Response response;

    if (oAuthheader == null || oAuthheader.isEmpty() && mBearerToken != null) {
      Request request =
          original.newBuilder().header(OAuthService.HEADER_AUTHORIZATION, mBearerToken).build();
      response = chain.proceed(request);
    } else {
      response = chain.proceed(original);
    }
    return response;
  }

  private int responseCount(Response response) {
    int count = 0;
    while ((response = response.priorResponse()) != null) {
      count++;
    }
    return count;
  }

  public void logout() {
    set(null);
  }
}
