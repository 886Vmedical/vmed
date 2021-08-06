package com.mediatek.mt6381.ble.command;

import android.util.Log;

import com.mediatek.mt6381.ble.GattUUID;
import java.util.UUID;

public class SetDeviceNameCommand extends BaseCommand {

  private static final int TYPE = 0x70;
  private final String mName;

  public SetDeviceNameCommand(String name) {
    mName = name;
  }

  @Override public byte[] getBytes() {
    byte[] nameBytes = mName.getBytes();
    byte[] ret = new byte[nameBytes.length + 1];
    ret[0] = TYPE;
    System.arraycopy(nameBytes, 0, ret, 1, nameBytes.length);
    return ret;
  }

  @Override public UUID getWriteCharacteristicUUID() {
    Log.d("SetDeviceNameCommand:" , "getWriteCharacteristicUUID: " + GattUUID.Characteristic.Command.getUuid());
    return GattUUID.Characteristic.Command.getUuid();
  }

  @Override public int getType() {
    Log.d("SetDeviceNameCommand:" , "TYPE: " + TYPE);
    return TYPE;
  }
}
