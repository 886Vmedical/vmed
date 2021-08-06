package com.mediatek.mt6381eco.biz.history;

import dagger.Binds;
import dagger.Module;

@Module public abstract class HRVHistoryModule {

  @Binds abstract HistoryContract.View provideView(HRVHistoryActivity activity);

  @Binds abstract HistoryContract.Presenter providePresenter(HistoryPresenter presenter);
}
