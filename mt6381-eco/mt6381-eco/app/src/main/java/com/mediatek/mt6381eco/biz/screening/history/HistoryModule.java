package com.mediatek.mt6381eco.biz.screening.history;

import dagger.Binds;
import dagger.Module;

@Module abstract class HistoryModule {
  @Binds abstract HistoryContract.View provideView(HistoryFragment activity);

  @Binds abstract HistoryContract.Presenter providePresenter(HistoryPresenter presenter);
}
