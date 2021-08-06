package com.mediatek.mt6381eco.biz.connect;

import com.mediatek.mt6381eco.biz.peripheral.IPeripheral;
import com.mediatek.mt6381eco.mvp.BasePresenter2;

public interface IPresenter extends BasePresenter2{
  void requestConnect(String address);

  void attach(IPeripheral service);
}
