package com.mediatek.mt6381eco.biz.connect;

import dagger.Binds;
import dagger.Module;

@Module public abstract class ConnectModule {
  @Binds abstract IPresenter providePresenter(ConnectPresenter presenter2);
}
