package com.mediatek.mt6381eco.biz.history;

import com.mediatek.mt6381eco.dagger.ActivityScoped;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@ActivityScoped @Subcomponent(modules = { BPHistoryModule.class })
public interface BPHistoryComponent extends AndroidInjector<BPHistoryActivity> {

  @Subcomponent.Builder abstract class Builder extends AndroidInjector.Builder<BPHistoryActivity> {
  }
}

