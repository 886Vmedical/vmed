package com.mediatek.mt6381eco.biz.history;

import com.mediatek.mt6381eco.dagger.ActivityScoped;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@ActivityScoped @Subcomponent(modules = { HRVHistoryModule.class })
public interface HRVHistoryComponent extends AndroidInjector<HRVHistoryActivity> {

  @Subcomponent.Builder abstract class Builder extends AndroidInjector.Builder<HRVHistoryActivity> {
  }
}

