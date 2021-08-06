package com.mediatek.mt6381eco.biz.temp;


import com.mediatek.mt6381eco.dagger.ActivityScoped;
import dagger.Binds;
import dagger.Module;

    @Module
    public abstract class TempModule {
        @Binds
        abstract TempContract.View provideView(TemperatureActivity activity);

        @Binds @ActivityScoped
        abstract TempContract.Presenter providePresenter(TempPresenter presenter);


}
