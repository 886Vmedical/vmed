package com.mediatek.mt6381eco.biz.account.signin;

import com.mediatek.mt6381eco.biz.account.AccountContract;
import com.mediatek.mt6381eco.dagger.ActivityScoped;
import dagger.Binds;
import dagger.Module;

@Module public abstract class SignInModule {
  @Binds abstract AccountContract.View provideView(SignInFragment fragment);

  @Binds
  abstract AccountContract.Presenter providePresenter(SignInPresenter presenter);
}
