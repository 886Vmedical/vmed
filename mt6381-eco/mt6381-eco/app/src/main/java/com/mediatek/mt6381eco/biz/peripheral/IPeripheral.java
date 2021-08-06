package com.mediatek.mt6381eco.biz.peripheral;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

public interface IPeripheral {
  int STATE_DISCONNECTED = 0;
  int STATE_CONNECTEDING = 1;
  int STATE_DISCONNECTING = 2;
  int STATE_CONNECTED = 3;

  Completable connect();
  void disconnect();

  int getConnectionState();

  Flowable<Integer> onConnectionChange();

  Completable startMeasure(boolean downSample);

  Completable stopMeasure();

  Completable startThroughputTest();

  long getThroughput();

  Single<DeviceInfo> readDeviceInfo();

  //add by herman for temp.
  Completable readTemperature();
  Completable calibrationTemperature();
  //end

  Completable sendMeasureFinish();

}
