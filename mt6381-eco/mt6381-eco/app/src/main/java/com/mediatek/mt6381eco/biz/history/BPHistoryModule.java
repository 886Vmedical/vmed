package com.mediatek.mt6381eco.biz.history;

import dagger.Binds;
import dagger.Module;

@Module public abstract class BPHistoryModule {

  @Binds abstract HistoryContract.View provideView(BPHistoryActivity activity);

  @Binds abstract HistoryContract.Presenter providePresenter(HistoryPresenter presenter);
}
