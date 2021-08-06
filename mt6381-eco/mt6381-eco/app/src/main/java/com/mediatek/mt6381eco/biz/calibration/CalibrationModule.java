package com.mediatek.mt6381eco.biz.calibration;

import dagger.Binds;
import dagger.Module;

@Module public abstract class CalibrationModule {


  @Binds abstract CalibrationContract.Presenter providePresenter(CalibrationPresenter presenter);
}
