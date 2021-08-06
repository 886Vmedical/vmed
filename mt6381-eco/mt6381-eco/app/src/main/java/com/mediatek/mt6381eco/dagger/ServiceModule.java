package com.mediatek.mt6381eco.dagger;

import com.mediatek.mt6381eco.biz.peripheral.PeripheralService;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ServiceModule {
  @ServiceScoped
  @ContributesAndroidInjector()
  abstract PeripheralService contributePeripheralService();
}
