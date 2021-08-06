package com.mediatek.mt6381eco.biz.history;

import com.mediatek.mt6381eco.dagger.ActivityScoped;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@ActivityScoped @Subcomponent(modules = { HistoryModule.class }) public interface HistoryComponent
    extends AndroidInjector<HistoryActivity> {

  @Subcomponent.Builder abstract class Builder extends AndroidInjector.Builder<HistoryActivity> {
  }
}

