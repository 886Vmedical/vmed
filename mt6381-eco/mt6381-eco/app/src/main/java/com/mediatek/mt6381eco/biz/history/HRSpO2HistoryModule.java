package com.mediatek.mt6381eco.biz.history;

import dagger.Binds;
import dagger.Module;

@Module public abstract class HRSpO2HistoryModule {

  @Binds abstract HistoryContract.View provideView(HRSpO2HistoryActivity activity);

  @Binds abstract HistoryContract.Presenter providePresenter(HistoryPresenter presenter);
}
