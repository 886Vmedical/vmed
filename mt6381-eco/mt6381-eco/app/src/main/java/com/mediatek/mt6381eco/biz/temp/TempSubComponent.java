package com.mediatek.mt6381eco.biz.temp;

import com.mediatek.mt6381eco.dagger.ActivityScoped;

import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@ActivityScoped @Subcomponent(modules = {TempModule.class}) public interface TempSubComponent
    extends AndroidInjector<TemperatureActivity> {
  @Subcomponent.Builder abstract class Builder extends AndroidInjector.Builder<TemperatureActivity> {
  }
}
