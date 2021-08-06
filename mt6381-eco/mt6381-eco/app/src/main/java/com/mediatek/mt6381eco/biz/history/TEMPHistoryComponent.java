package com.mediatek.mt6381eco.biz.history;

import com.mediatek.mt6381eco.dagger.ActivityScoped;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@ActivityScoped @Subcomponent(modules = { TEMPHistoryModule.class })
public interface TEMPHistoryComponent extends AndroidInjector<TEMPHistoryActivity> {

  @Subcomponent.Builder abstract class Builder extends AndroidInjector.Builder<TEMPHistoryActivity> {
  }
}

