package com.mediatek.blenativewrapper.commnication;

import android.bluetooth.BluetoothGattCharacteristic;

public class ReadCharacteristicCommunication extends BaseCommunicationItem {
  public ReadCharacteristicCommunication(BluetoothGattCharacteristic characteristic) {
    super(TYPE_READ_CHARACTERISTIC, characteristic);
  }
}
