package com.mediatek.mt6381eco.biz.profile;

import com.mediatek.mt6381eco.dagger.ActivityScoped;
import dagger.Binds;
import dagger.Module;

@Module
abstract class ProfileModule {
  @Binds
  abstract ProfileContract.View provideView(ProfileActivity activity);

  @Binds @ActivityScoped abstract ProfileContract.Presenter providePresenter(ProfilePresenter presenter);


}
