package com.mediatek.mt6381eco.biz.peripheral;

import io.reactivex.Completable;
import java.util.UUID;

public interface IBlePeripheral {
  String getName();
  Completable setDeviceName(String deviceName);

  Completable writeCharacteristic(UUID uuid, boolean noResponse,byte[] data);
}
