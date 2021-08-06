package com.mediatek.mt6381eco.biz.temp;

import com.mediatek.mt6381eco.mvp.BasePresenter2;
import com.mediatek.mt6381eco.mvp.BaseView;

import io.reactivex.Completable;

public interface TempContract {

    interface View extends BaseView {

        double getOneUploadTemp() ;
        // void navToNext();
    }

    interface Presenter extends BasePresenter2<View> {

       // Completable requestSaveProfile();
       // void requestSaveProfileForSB();
       void onUploadtemp();
    }



}
