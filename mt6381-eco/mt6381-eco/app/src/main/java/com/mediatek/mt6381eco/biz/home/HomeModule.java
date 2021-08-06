package com.mediatek.mt6381eco.biz.home;

import com.mediatek.mt6381eco.dagger.ActivityScoped;
import dagger.Binds;
import dagger.Module;

@Module
abstract class HomeModule {
  @Binds abstract HomeContract.View provideView(HomeActivity activity);

  @Binds @ActivityScoped abstract HomeContract.Presenter providePresenter(HomePresenter presenter);
}
