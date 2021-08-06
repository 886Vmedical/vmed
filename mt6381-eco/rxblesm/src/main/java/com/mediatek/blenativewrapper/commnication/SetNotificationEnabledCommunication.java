package com.mediatek.blenativewrapper.commnication;

import android.bluetooth.BluetoothGattCharacteristic;
import lombok.Getter;

@Getter
public class SetNotificationEnabledCommunication extends BaseCommunicationItem {

  private final boolean enable;

  public SetNotificationEnabledCommunication(BluetoothGattCharacteristic characteristic, boolean enable) {
    super(TYPE_SET_NOTIFICATION,characteristic);
    this.enable = enable;
  }
}
