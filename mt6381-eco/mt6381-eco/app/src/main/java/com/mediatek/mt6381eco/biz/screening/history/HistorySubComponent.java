package com.mediatek.mt6381eco.biz.screening.history;

import com.mediatek.mt6381eco.dagger.FragmentScoped;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@FragmentScoped @Subcomponent(modules = { HistoryModule.class })
public interface HistorySubComponent extends AndroidInjector<HistoryFragment> {
  @Subcomponent.Builder abstract class Builder extends AndroidInjector.Builder<HistoryFragment> {
  }
}
