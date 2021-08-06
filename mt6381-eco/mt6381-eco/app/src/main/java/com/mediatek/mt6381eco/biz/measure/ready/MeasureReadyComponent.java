package com.mediatek.mt6381eco.biz.measure.ready;

import com.mediatek.mt6381eco.dagger.ActivityScoped;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@ActivityScoped @Subcomponent(modules = { MeasureReadyModule.class })
public interface MeasureReadyComponent extends AndroidInjector<MeasureReadyActivity> {

  @Subcomponent.Builder abstract class Builder
      extends AndroidInjector.Builder<MeasureReadyActivity> {
  }
}
