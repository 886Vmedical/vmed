package com.mediatek.mt6381eco.biz.home;

import com.mediatek.mt6381eco.dagger.ActivityScoped;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@ActivityScoped @Subcomponent(modules = { HomeModule.class })
public interface HomeSubComponent extends AndroidInjector<HomeActivity> {
  @Subcomponent.Builder abstract class Builder extends AndroidInjector.Builder<HomeActivity> {
  }
}
