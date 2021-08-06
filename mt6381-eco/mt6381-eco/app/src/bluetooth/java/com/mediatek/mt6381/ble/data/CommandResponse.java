package com.mediatek.mt6381.ble.data;

import java.io.IOException;
import java.util.Locale;
import lombok.Getter;

@Getter
public class CommandResponse extends BaseData {
  private byte commandType;
  private byte response;

  public CommandResponse(byte[] data) throws IOException {
    super(data);
  }

  @Override protected void parse(byte[] data) throws IOException {
    if(data.length != 4){
      throw  new IOException("invalid data length:" + data.length);
    }
    commandType = data[1];
    response = data[2];
  }

  public boolean isSuccess(){
    return response == 0x01;
  }

  @Override public String toString() {
    return String.format(Locale.getDefault(), "%s: %d-%d", getClass().getSimpleName(), commandType, response);
  }
}
