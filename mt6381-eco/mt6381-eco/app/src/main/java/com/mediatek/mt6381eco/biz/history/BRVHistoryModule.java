package com.mediatek.mt6381eco.biz.history;

import dagger.Binds;
import dagger.Module;

@Module public abstract class BRVHistoryModule {

  @Binds abstract HistoryContract.View provideView(BRVHistoryActivity activity);

  @Binds abstract HistoryContract.Presenter providePresenter(HistoryPresenter presenter);
}
