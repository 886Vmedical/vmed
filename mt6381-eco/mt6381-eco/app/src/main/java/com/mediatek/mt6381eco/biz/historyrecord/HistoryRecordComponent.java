package com.mediatek.mt6381eco.biz.historyrecord;

import com.mediatek.mt6381eco.dagger.ActivityScoped;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@ActivityScoped @Subcomponent(modules = { HistoryRecordModule.class })
public interface HistoryRecordComponent extends AndroidInjector<HistoryRecordActivity> {

  @Subcomponent.Builder abstract class Builder
      extends AndroidInjector.Builder<HistoryRecordActivity> {
  }
}
