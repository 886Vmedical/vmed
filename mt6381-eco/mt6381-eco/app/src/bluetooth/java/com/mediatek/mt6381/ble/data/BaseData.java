package com.mediatek.mt6381.ble.data;

import java.io.IOException;

public abstract class BaseData {
  public BaseData(byte[] data) throws IOException {
   parse(data);
  }

  protected abstract void parse(byte[] data) throws IOException;


}
