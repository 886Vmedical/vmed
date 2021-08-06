package com.mediatek.mt6381.ble.command;

import java.util.UUID;

public abstract class BaseCommand {
  public abstract byte[] getBytes();
  public abstract UUID getWriteCharacteristicUUID();
  public abstract int getType();
}
