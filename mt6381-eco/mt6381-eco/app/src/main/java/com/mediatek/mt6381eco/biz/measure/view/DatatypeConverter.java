package com.mediatek.mt6381eco.biz.measure.view;

import java.util.List;

public class DatatypeConverter {
  final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

  public static byte[] hexStringToByteArray(String s) {
    int len = s.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] =
          (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
    }
    return data;
  }

  public static int byte4ToInt(byte[] b) {
    return b[0] & 0xFF | (b[1] & 0xFF) << 8 | (b[2] & 0xFF) << 16 | (b[3] & 0xFF) << 24;
  }

  public static String bytesToHex(byte[] bytes) {
    char[] hexChars = new char[bytes.length * 2];
    for (int j = 0; j < bytes.length; j++) {
      int v = bytes[j] & 0xFF;
      hexChars[j * 2] = hexArray[v >>> 4];
      hexChars[j * 2 + 1] = hexArray[v & 0x0F];
    }
    return new String(hexChars);
  }

  public static float ecgConvertToMv(int data_ecg) {
    float ekg_vpp = 4; // unit: bits
    float ekg_bits = 23; // unit: bits
    float ekg_adc_lsb = (float) (ekg_vpp / Math.pow(2, ekg_bits));
    float ekg_gain = 6; // 6: ekg measurement
    if (data_ecg > Math.pow(2, 22)) {
      data_ecg -= Math.pow(2, 23); //unsigned -> signed
    }
    return data_ecg * ekg_adc_lsb * 1000.0f / ekg_gain;
  }

  public static float ppg1ConvertToMv(int data_ppg1) {
    float ppg_vpp = 3.2f; // unit: volt
    float ppg_bits = 16; // unit: bits
    float ppg_adc_lsb = (float) (ppg_vpp / Math.pow(2, ppg_bits));
    float ppg_tia_gain = 1;
    float ppg_pga_gain = 1;

    if (data_ppg1 > Math.pow(2, 22)) data_ppg1 -= Math.pow(2, 23);
    return data_ppg1 * ppg_adc_lsb * 1000.0f / (ppg_tia_gain * ppg_pga_gain); // convert to mV
  }

  public static int byteArrayToInt(byte[] b) {
    int value = 0;
    for (int i = 0; i < 4; i++) {
      int shift = (4 - 1 - i) * 8;
      value += (b[i] & 0x000000FF) << shift;
    }
    return value;
  }

  public static String bytesToIntString(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    if (bytes.length > 0) {
      int data = 0;
      int index = 0;
      int data_length = bytes.length;
      boolean isFirst = true;
      // Log.e(TAG, DatatypeConverter.bytesToHex(bytes));
                        /* transfer byte into integer */
      while (data_length - 4 >= 0) {
        byte[] tmp = new byte[4];
        tmp[0] = bytes[index + 3];
        tmp[1] = bytes[index + 2];
        tmp[2] = bytes[index + 1];
        tmp[3] = bytes[index];
        data = byteArrayToInt(tmp);
        index += 4;
        data_length -= 4;
        if (!isFirst) {
          sb.append(",");
        }
        sb.append(data);
        isFirst = false;
      }
    }
    // Log.v("btData", readMessage);
    return sb.toString();
  }

  public static int[] bytesToIntArray(byte[] bytes) {
    int[] result = new int[bytes.length / 4];
    int data_length = bytes.length;
    int i = 0;
    int j = 0;
    while (j + 4 <= data_length) {
      result[i++] = (bytes[j++] & 0xFF)
          | ((bytes[j++] & 0xFF) << 8)
          | ((bytes[j++] & 0xFF) << 16)
          | ((bytes[j++] & 0xFF) << 24);
    }
    //Log.v("btdata", intArrayToString(result, 0));
    return result;
  }

  public static String intArrayToString(int[] ints, int startIndex) {
    StringBuilder stringBuilder = new StringBuilder();
    if (ints.length > startIndex) {
      stringBuilder.append(ints[startIndex]);

      for (int i = startIndex + 1; i < ints.length; ++i) {
        stringBuilder.append(",");
        stringBuilder.append(ints[i]);
      }
    }
    return stringBuilder.toString();
  }

  public static int[] convertIntegers(List<Integer> integers) {
    int[] ret = new int[integers.size()];
    for (int i = 0; i < ret.length; i++) {
      ret[i] = integers.get(i).intValue();
    }
    return ret;
  }
}
