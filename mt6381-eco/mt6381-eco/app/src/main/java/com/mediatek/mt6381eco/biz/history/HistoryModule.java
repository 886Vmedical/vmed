package com.mediatek.mt6381eco.biz.history;

import com.mediatek.mt6381eco.dagger.ActivityScoped;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@Module public abstract class HistoryModule {

  @Provides @ActivityScoped static HistoryViewModel provideViewModel() {
    return new HistoryViewModel();
  }

  @Binds abstract HistoryContract.View provideView(HistoryActivity activity);

  @Binds abstract HistoryContract.Presenter providePresenter(HistoryPresenter presenter);
}
