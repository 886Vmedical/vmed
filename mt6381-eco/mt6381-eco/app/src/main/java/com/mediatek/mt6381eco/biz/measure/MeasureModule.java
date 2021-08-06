package com.mediatek.mt6381eco.biz.measure;

import dagger.Binds;
import dagger.Module;

@Module public abstract class MeasureModule {

  @Binds abstract MeasureContract.Presenter providePresenter(MeasurePresenter presenter);
}
