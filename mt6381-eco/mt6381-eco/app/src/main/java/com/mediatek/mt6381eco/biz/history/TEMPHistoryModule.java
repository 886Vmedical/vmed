package com.mediatek.mt6381eco.biz.history;

import dagger.Binds;
import dagger.Module;

@Module public abstract class TEMPHistoryModule {

  @Binds abstract HistoryContract.View provideView(TEMPHistoryActivity activity);

  @Binds abstract HistoryContract.Presenter providePresenter(HistoryPresenter presenter);
}
