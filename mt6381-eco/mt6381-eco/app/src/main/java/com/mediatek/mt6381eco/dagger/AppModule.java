package com.mediatek.mt6381eco.dagger;

import android.app.Application;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mediatek.mt6381eco.MApplication;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

@Module public class AppModule {

  @Provides @Singleton Application provideApplication(MApplication application) {
    return application;
  }

  @Provides @Singleton Gson provideGson() {
    GsonBuilder gsonBuilder = new GsonBuilder();
    gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.IDENTITY);
    return gsonBuilder.create();
  }


}
