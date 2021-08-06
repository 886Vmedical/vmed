package com.mediatek.blenativewrapper.commnication;

import android.bluetooth.BluetoothGattCharacteristic;
import java.util.Iterator;
import lombok.Getter;

@Getter public class WriteCharacteristicCommunicationItem extends BaseCommunicationItem {
  private boolean noResponse = false;
  private final byte[] data;

  public WriteCharacteristicCommunicationItem(BluetoothGattCharacteristic characteristic,
      boolean noResponse, byte[] data) {
    super(TYPE_WRITE_CHARACTERISTIC, characteristic);
    this.noResponse = noResponse;
    this.data = data;
  }

  public Iterator<WriteCharacteristicCommunicationItem> split(int packageLength){
    return new Iterator<WriteCharacteristicCommunicationItem>() {
      int pos = 0;

      @Override public boolean hasNext() {
        return pos < data.length;
      }

      @Override public WriteCharacteristicCommunicationItem next() {
        int length = Math.min(data.length - pos, packageLength);
        byte[] temp = new byte[length];
        System.arraycopy(data, 0, temp, 0, length);
        pos += length;
        return new WriteCharacteristicCommunicationItem(getCharacteristic(), noResponse, temp);
      }
    };
  }

}
