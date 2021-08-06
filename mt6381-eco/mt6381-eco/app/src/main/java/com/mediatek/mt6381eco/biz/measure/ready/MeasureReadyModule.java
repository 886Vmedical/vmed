package com.mediatek.mt6381eco.biz.measure.ready;

import dagger.Binds;
import dagger.Module;

@Module public abstract class MeasureReadyModule {

  @Binds abstract MeasureReadyContract.Presenter providePresenter(MeasureReadyPresenter presenter);
}


