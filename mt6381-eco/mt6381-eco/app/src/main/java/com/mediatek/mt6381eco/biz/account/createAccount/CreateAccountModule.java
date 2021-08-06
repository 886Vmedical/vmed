package com.mediatek.mt6381eco.biz.account.createAccount;

import com.mediatek.mt6381eco.biz.account.AccountContract;
import com.mediatek.mt6381eco.dagger.ActivityScoped;
import dagger.Binds;
import dagger.Module;

@Module
public abstract class CreateAccountModule {
  @Binds
  abstract AccountContract.View provideView(CreateAccountFragment fragment);

  @Binds abstract AccountContract.Presenter providePresenter(CreateAccountPresenter presenter);


}
