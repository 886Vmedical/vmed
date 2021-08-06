package com.mediatek.blenativewrapper.commnication;

import android.bluetooth.BluetoothGattCharacteristic;
import lombok.Getter;

@Getter
public abstract class BaseCommunicationItem {
  public static final int TYPE_SET_NOTIFICATION = 1;
  public static final int TYPE_WRITE_CHARACTERISTIC = 2;
  public static final int TYPE_READ_CHARACTERISTIC = 3;

  private final int type;
  private final BluetoothGattCharacteristic characteristic;

  protected BaseCommunicationItem(int type, BluetoothGattCharacteristic characteristic) {
    this.type = type;
    this.characteristic = characteristic;
  }
}
