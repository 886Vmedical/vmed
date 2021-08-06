package com.mediatek.mt6381eco.biz.history;

import com.mediatek.mt6381eco.dagger.ActivityScoped;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@ActivityScoped @Subcomponent(modules = { HRSpO2HistoryModule.class })
public interface HRSpO2HistoryComponent extends AndroidInjector<HRSpO2HistoryActivity> {

  @Subcomponent.Builder abstract class Builder
      extends AndroidInjector.Builder<HRSpO2HistoryActivity> {
  }
}

