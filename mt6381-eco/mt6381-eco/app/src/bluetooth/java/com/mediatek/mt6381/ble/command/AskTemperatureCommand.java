package com.mediatek.mt6381.ble.command;

import android.util.Log;
import android.view.Menu;

import com.mediatek.mt6381.ble.GattUUID;
import com.mediatek.mt6381eco.R;

import java.util.UUID;

public class AskTemperatureCommand extends BaseCommand {
  private static final int TYPE = 0x80;

  private static final byte data1 = (byte)(TYPE & 0xFF);
  private static final byte data2 = (byte)1;

  public AskTemperatureCommand(){
    getBytes();
    getType();

  }

  public static byte[] intToByteArray(int i) {
    byte[] result = new byte[4];
    result[0] = (byte)((i >> 24) & 0xFF);
    result[1] = (byte)((i >> 16) & 0xFF);
    result[2] = (byte)((i >> 8) & 0xFF);
    result[3] = (byte)(i & 0xFF);

    Log.d("AskTemperatureCommand:" ,"result[0]: " + result[0]);
    Log.d("AskTemperatureCommand:" ,"result[1]: " + result[1]);
    Log.d("AskTemperatureCommand:" ,"result[2]: " + result[2]);
    Log.d("AskTemperatureCommand:" ,"result[3]: " + result[3]);

    return result;
  }


  @Override public byte[] getBytes() {

      Log.d("AskTemperatureCommand:" ,"data1: " + data1);
      Log.d("AskTemperatureCommand:" , "data2: " + data2 );
      return new byte[] {data1 , 0x01};
      //return intToByteArray(128);

    }

  @Override public UUID getWriteCharacteristicUUID() {
    Log.d("AskTemperatureCommand:" , "getWriteCharacteristicUUID: " + GattUUID.Characteristic.Command.getUuid());
    return GattUUID.Characteristic.Command.getUuid();
  }

  @Override public int getType() {
    Log.d("AskTemperatureCommand:" , "TYPE: " + TYPE);
    return TYPE;
  }

}
