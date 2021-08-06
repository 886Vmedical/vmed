package com.mediatek.mt6381eco.biz.startup;

import com.mediatek.mt6381eco.dagger.ActivityScoped;
import dagger.Binds;
import dagger.Module;

@Module public abstract class StartupModule {
  @Binds abstract StartupContract.View provideView(StartupActivity activity);

  @Binds @ActivityScoped
  abstract StartupContract.Presenter providePresenter(StartupPresenter presenter);
}
