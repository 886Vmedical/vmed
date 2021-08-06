package com.mediatek.mt6381eco.network;

import android.app.Application;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.google.gson.Gson;
import com.mediatek.mt6381eco.AppConstants;
import com.mediatek.mt6381eco.BuildConfig;
import com.mediatek.mt6381eco.log.HttpLoggingInterceptor;
import dagger.Module;
import dagger.Provides;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import javax.inject.Singleton;
import lombok.Setter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

@Setter @Module public class NetworkModule {

  private static final String HEADER_ACCEPT_LANGUAGE = "accept-language";
  private static final long WRITE_TIMEOUT = 60;
  private final StethoInterceptor mStethoInterceptor = new StethoInterceptor();

  @Provides @Singleton ApiService provideApiService(OkHttpClient client, Gson gson) {
    Retrofit retrofit = new Retrofit.Builder().baseUrl(AppConstants.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxErrorHandlingCallAdapterFactory.create())
        .client(client)
        .build();

    return retrofit.create(ApiService.class);
  }

  @Provides OkHttpClient provideOkHttpClient(OAuthHelper oAuthHelper) {
    OkHttpClient.Builder builder =
        new OkHttpClient.Builder().writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS);
    if (BuildConfig.DEBUG) {
      builder.addNetworkInterceptor(mStethoInterceptor);
    }
    //accept-language
    builder.addInterceptor(chain -> {
      Request original = chain.request();
      String language = original.header(HEADER_ACCEPT_LANGUAGE);
      Response response;
      if (language == null || language.isEmpty()) {
        String country = Locale.getDefault().getCountry();
        Request request = original.newBuilder()
            .header(HEADER_ACCEPT_LANGUAGE,
                Locale.getDefault().getLanguage() + (country.isEmpty() ? "" : "-" + country))
            .build();
        response = chain.proceed(request);
      } else {
        response = chain.proceed(original);
      }
      return response;
    });
    builder.authenticator(oAuthHelper).addInterceptor(oAuthHelper);
    builder.addInterceptor(
        new HttpLoggingInterceptor(message -> Timber.tag("OkHttp").d(message), 1024 * 10).setLevel(
            HttpLoggingInterceptor.Level.BODY));
    return builder.build();
  }

  @Provides @Singleton NetworkCheck provideNetworkCheck(Application application) {
    return new NetworkCheck(application);
  }

  @Provides @Singleton OAuthService provideOAuthService(Gson gson) {
    OkHttpClient.Builder builder = new OkHttpClient.Builder();
    if (BuildConfig.DEBUG) {
      builder.addNetworkInterceptor(mStethoInterceptor);
    }

    builder.addInterceptor(
        new HttpLoggingInterceptor(message -> Timber.tag("OkHttp").d(message), 1024 * 10).setLevel(
            HttpLoggingInterceptor.Level.BODY));
    Retrofit retrofit = new Retrofit.Builder().baseUrl(AppConstants.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxErrorHandlingCallAdapterFactory.create())
        .client(builder.build())
        .build();
    return retrofit.create(OAuthService.class);
  }
}