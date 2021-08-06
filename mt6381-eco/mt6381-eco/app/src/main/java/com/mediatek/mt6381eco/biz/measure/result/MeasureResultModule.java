package com.mediatek.mt6381eco.biz.measure.result;

import com.mediatek.mt6381eco.dagger.ActivityScoped;
import dagger.Binds;
import dagger.Module;

@Module public abstract class MeasureResultModule {

  @Binds abstract MeasureResultContract.View provideView(MeasureResultActivity activity);

  @Binds @ActivityScoped
  abstract MeasureResultContract.Presenter providePresenter(MeasureResultPresenter presenter);
}


