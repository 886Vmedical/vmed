package com.mediatek.mt6381eco.dagger;

import com.mediatek.mt6381eco.MApplication;
import com.mediatek.mt6381eco.db.DatabaseModule;
import com.mediatek.mt6381eco.network.NetworkModule;
import dagger.Component;
import dagger.android.AndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;
import javax.inject.Singleton;

@Singleton @Component(modules = {
    AppModule.class, ActivityModule.class, NetworkModule.class, DatabaseModule.class,
    AndroidSupportInjectionModule.class, FlavorModule.class, FragmentModule.class, BindModule.class
    ,ServiceModule.class
}) interface AppComponent extends AndroidInjector<MApplication> {

  @Component.Builder abstract class Builder extends AndroidInjector.Builder<MApplication> {

  }
}
