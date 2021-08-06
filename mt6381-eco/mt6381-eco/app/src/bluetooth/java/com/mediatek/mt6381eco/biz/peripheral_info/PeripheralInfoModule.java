package com.mediatek.mt6381eco.biz.peripheral_info;

import dagger.Binds;
import dagger.Module;

@Module
public abstract class PeripheralInfoModule {

  @Binds abstract PeripheralInfoContract.Presenter providePresenter(PeripheralInfoPresenter presenter);

}
