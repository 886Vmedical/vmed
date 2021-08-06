package com.mediatek.mt6381.ble.command;

import android.util.Log;

import com.mediatek.mt6381.ble.GattUUID;
import java.util.UUID;

public class SetTimeoutCommand extends BaseCommand {

  private static final int TYPE = 0x21;
  private final int mTimeout;

  public SetTimeoutCommand(int timeout) {
    mTimeout = timeout & 0xFF;
  }

  @Override public byte[] getBytes() {
    return new byte[] { TYPE , (byte) mTimeout };
  }

  @Override public UUID getWriteCharacteristicUUID() {
    return GattUUID.Characteristic.Command.getUuid();
  }


  @Override public int getType() {
    Log.d("SetTimeoutCommand:" , "TYPE: " + TYPE);
    return TYPE;
  }

}
