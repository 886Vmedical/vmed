package com.mediatek.mt6381.ble;

import com.mediatek.blenativewrapper.BLEDataReceiver;
import com.mediatek.blenativewrapper.rxbus.RxBus;
import com.mediatek.blenativewrapper.utils.DataConvertUtils;
import com.mediatek.mt6381.ble.data.CommandResponse;
import com.mediatek.mt6381.ble.data.SystemInformationData;
import java.io.IOException;
import java.util.UUID;
import timber.log.Timber;

public class MT6381SystemInfoParser implements BLEDataReceiver {
  private final byte[] mBuffer = new byte[300];
  private int position = 0;
  private int packageLength = -1;
  public static String parseData = " ";

  @Override public void reset() {
    packageLength = -1;
    position = 0;
  }

  @Override public void receive(UUID uuid, byte[] data, int len) {
    if (position + data.length > mBuffer.length) {
      int shiftLen = position + data.length - mBuffer.length;
      System.arraycopy(mBuffer, shiftLen, mBuffer, 0, mBuffer.length - shiftLen);
      position -= shiftLen;
    }
    System.arraycopy(data, 0, mBuffer, position, data.length);
    position += data.length;
    doParse();
  }

  @Override public void destroy() {

  }

  private void doParse() {
    if (packageLength == -1) {
      switch ((int) mBuffer[0]) {
        case 0x00: {
          packageLength = 6;
          break;
        }
        case 0b00111111: {
          packageLength = 4;
          break;
        }
      }
    }
    if (packageLength > 0) {
      if (position >= packageLength) {
        try {
          byte[] temp = new byte[packageLength];
          System.arraycopy(mBuffer, position - packageLength, temp, 0, packageLength);
          //todo by herman
          parseData = DataConvertUtils.bytesToHex(temp);
          Timber.d("parseData:%s", DataConvertUtils.bytesToHex(temp));//parseData:00-00-3D-9A-FA-0F
          postDataObject(temp);
          System.arraycopy(mBuffer, position, mBuffer, 0, mBuffer.length - packageLength);
          position -= packageLength;
          packageLength = -1;
        } catch (IOException e) {
          Timber.e(e, e.getMessage());
        }
      }
    }
  }

  private void postDataObject(byte[] mBuffer) throws IOException {
    switch ((int) mBuffer[0]) {
      case 0x10:
      case 0x00: {
        RxBus.getInstance().post(new SystemInformationData(mBuffer));
        break;
      }
      case 0b00111111: {
        RxBus.getInstance().post(new CommandResponse(mBuffer));
        break;
      }
    }
  }
}
