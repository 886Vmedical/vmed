package com.mediatek.mt6381eco.biz.startup;

import com.mediatek.mt6381eco.dagger.ActivityScoped;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@ActivityScoped @Subcomponent(modules = {StartupModule.class}) public interface StartupSubComponent
    extends AndroidInjector<StartupActivity> {


  @Subcomponent.Builder abstract class Builder extends AndroidInjector.Builder<StartupActivity> {
  }
}
