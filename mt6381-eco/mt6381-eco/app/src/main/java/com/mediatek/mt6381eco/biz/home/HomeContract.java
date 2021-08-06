package com.mediatek.mt6381eco.biz.home;

import com.mediatek.mt6381eco.biz.peripheral.IPeripheral;
import com.mediatek.mt6381eco.mvp.BasePresenter2;
import com.mediatek.mt6381eco.mvp.BaseView;
import io.reactivex.Completable;

public interface HomeContract {

  interface View extends BaseView {

    void navToStartup();
  }

  interface Presenter extends BasePresenter2<View> {

    void attach(IPeripheral peripheral);

    void disconnect();

    Completable requestSignOut();

    void downgrade();

    void upgrade();

    void deleteCalibration();
  }
}
