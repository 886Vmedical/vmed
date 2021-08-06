package com.mediatek.mt6381eco.biz.peripheral_info;

import com.mediatek.mt6381eco.biz.peripheral.IPeripheral;
import com.mediatek.mt6381eco.mvp.BasePresenter2;
import com.mediatek.mt6381eco.mvp.BaseView;

public interface PeripheralInfoContract {
  interface View extends BaseView{

  }

  interface Presenter extends BasePresenter2<View> {

    void attach(IPeripheral peripheral);
    PeripheralInfoViewModel getViewModel();

    void disconnect();

    void changeName(String name);

    void startDownload();

    void cancelDownload();

    void startInstall();
  }
}
