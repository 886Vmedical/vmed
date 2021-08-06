package com.mediatek.mt6381eco.biz.screening;

import com.mediatek.mt6381eco.dagger.ActivityScoped;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@ActivityScoped @Subcomponent(modules = { ScreeningModule.class })
public interface ScreeningSubComponent extends AndroidInjector<ScreeningActivity> {
  @Subcomponent.Builder abstract class Builder extends AndroidInjector.Builder<ScreeningActivity> {
  }
}
