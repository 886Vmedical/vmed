package com.mediatek.mt6381eco.biz.history;

import com.mediatek.mt6381eco.dagger.ActivityScoped;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@ActivityScoped @Subcomponent(modules = { BRVHistoryModule.class })
public interface BRVHistoryComponent extends AndroidInjector<BRVHistoryActivity> {

  @Subcomponent.Builder abstract class Builder extends AndroidInjector.Builder<BRVHistoryActivity> {
  }
}

