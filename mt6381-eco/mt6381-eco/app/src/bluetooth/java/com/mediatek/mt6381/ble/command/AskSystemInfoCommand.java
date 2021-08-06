package com.mediatek.mt6381.ble.command;

import android.util.Log;

import com.mediatek.mt6381.ble.GattUUID;
import java.util.UUID;

public class AskSystemInfoCommand extends BaseCommand {
  private static final int TYPE = 0x01;


  @Override public byte[] getBytes() {
    Log.d("AskSystemInfoCommand:" , "getBytes: ");
    return new byte[] { TYPE , 0x00};
  }

  @Override public UUID getWriteCharacteristicUUID() {
    Log.d("AskSystemInfoCommand:" , "getWriteCharacteristicUUID: "+ GattUUID.Characteristic.Command.getUuid());
    return GattUUID.Characteristic.Command.getUuid();
  }

  @Override public int getType() {
    Log.d("AskSystemInfoCommand:" , "TYPE: " + TYPE);
    return TYPE;
  }

}
