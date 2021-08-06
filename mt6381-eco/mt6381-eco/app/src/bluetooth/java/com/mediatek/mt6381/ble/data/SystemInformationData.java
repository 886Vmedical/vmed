package com.mediatek.mt6381.ble.data;

import android.content.Intent;
import android.util.Log;

import com.mediatek.mt6381eco.biz.measure.view.MContextCompat;

import java.io.IOException;
import java.util.Locale;
import lombok.Getter;

@Getter public class SystemInformationData extends BaseData {

  public byte mBattery;
  private String mFirmware;
  private int mMTUSize;
  private int mConnectionInterval;

  public SystemInformationData(byte[] data) throws IOException {
    super(data);
  }

  @Override protected void parse(byte[] data) throws IOException {

    if (data.length < 4) {
      throw new IOException("invalid data length =" + data.length);
    }
    mBattery = data[1];

    //todo by herman
    mFirmware = String.format(Locale.US, "%d.%d", data[2], data[3]);
    Log.d("SystemInformationData","data[2]: " + data[2]);
    Log.d("SystemInformationData","data[3]: " + data[3]);//有时为+，有时为-；

    //tody by herman
    Log.d("SystemInformationData","mBattery: " + mBattery);
    Log.d("SystemInformationData","mFirmware: " + mFirmware);

    if(data.length >4) {
      mMTUSize = data[4] & 0xff;
      mConnectionInterval = data[5] & 0xff;
    }

  }

  public int getThroughputBps(){
    return (int) (1000f / mConnectionInterval /2 * mMTUSize);
  }

  @Override public String toString() {
    return String.format(Locale.getDefault(), "%s: Battery:%d%% Firmware:%s ,Mtu:%d, Interval:%d",
        getClass().getSimpleName(), mBattery, mFirmware, mMTUSize, mConnectionInterval);
  }


}
