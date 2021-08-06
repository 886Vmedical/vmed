package com.mediatek.mt6381eco.biz.recorddetail;

import dagger.Binds;
import dagger.Module;

@Module public abstract class RecordDetailModule {
  @Binds abstract RecordDetailContract.Presenter providePresenter(RecordDetailPresenter presenter);
}
