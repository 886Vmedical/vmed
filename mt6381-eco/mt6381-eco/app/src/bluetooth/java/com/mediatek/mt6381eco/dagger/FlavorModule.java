package com.mediatek.mt6381eco.dagger;

import com.mediatek.mt6381eco.biz.connect.ConnectModule;
import com.mediatek.mt6381eco.biz.connect.ConnectFragment;
import com.mediatek.mt6381eco.biz.peripheral_info.PeripheralInfoFragment;
import com.mediatek.mt6381eco.biz.peripheral_info.PeripheralInfoModule;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module(subcomponents = {

}) public abstract class FlavorModule {

  @FragmentScoped @ContributesAndroidInjector(modules = PeripheralInfoModule.class)
  abstract PeripheralInfoFragment contributeSearchFragment();

  @FragmentScoped @ContributesAndroidInjector(modules = ConnectModule.class)
  abstract ConnectFragment contributeConnect2Fragment();
}
