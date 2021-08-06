package com.mediatek.mt6381eco.biz.historyrecord;

import dagger.Binds;
import dagger.Module;

@Module public abstract class HistoryRecordModule {

  @Binds abstract HistoryRecordContract.View provideView(HistoryRecordActivity activity);

  @Binds
  abstract HistoryRecordContract.Presenter providePresenter(HistoryRecordPresenter presenter);
}
