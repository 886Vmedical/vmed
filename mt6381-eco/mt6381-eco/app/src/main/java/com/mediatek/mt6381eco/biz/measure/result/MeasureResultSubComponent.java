package com.mediatek.mt6381eco.biz.measure.result;

import com.mediatek.mt6381eco.dagger.ActivityScoped;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@ActivityScoped @Subcomponent(modules = { MeasureResultModule.class })
public interface MeasureResultSubComponent extends AndroidInjector<MeasureResultActivity> {

  @Subcomponent.Builder abstract class Builder
      extends AndroidInjector.Builder<MeasureResultActivity> {
  }
}
