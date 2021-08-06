package com.mediatek.mt6381.ble.command;

import android.util.Log;

import com.mediatek.mt6381.ble.GattUUID;
import java.util.UUID;
import lombok.Getter;

public class MeasurementCommand extends BaseCommand{
  private static final int TYPE = 0x11;

  public static final int EKG_FLAG =            0b00000100;//4
  public static final int PPG1_FLAG =           0b00000010;//2
  public static final int PPG2_FLAG =           0b00000001;//1
  public static final int DEBUG_FLAG =           0b00010000;//16

  public static final int DOWN_SAMPLE_FLAG =           0b00100000;//32

  public static final int EKG_THROUGHPUT_FLAG = 0b00001000;//8
  @Getter
  private byte flag = 0;
  public MeasurementCommand(byte flag){
    this.flag = flag;
  }
  public static MeasurementCommand createOn(boolean downSample){
    byte value = (byte) (EKG_FLAG |PPG1_FLAG|PPG2_FLAG| DEBUG_FLAG | (downSample ? DOWN_SAMPLE_FLAG : 0 ));//hex:37  or 17.
    return new MeasurementCommand(value);
  }

  public static MeasurementCommand createThroughputOn(){
    return new MeasurementCommand((byte) EKG_THROUGHPUT_FLAG);//hex:8
  }

  public static MeasurementCommand createOff(){
    return new MeasurementCommand((byte) 0);//0
  }


 public void set(int flag){
    this.flag = (byte) (flag & 0xFF);
  }

  @Override public byte[] getBytes() {
    return new byte[]{TYPE, (byte) (flag & 0xFF) };
  }

  @Override public UUID getWriteCharacteristicUUID() {
      Log.d("MeasurementCommand:" , "getWriteCharacteristicUUID: " + GattUUID.Characteristic.Command.getUuid());
    return GattUUID.Characteristic.Command.getUuid();
  }

  @Override public int getType() {
    Log.d("MeasurementCommand:" , "TYPE: " + TYPE);
    return TYPE;
  }
}
