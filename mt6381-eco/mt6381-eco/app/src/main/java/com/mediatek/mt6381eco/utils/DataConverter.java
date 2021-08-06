package com.mediatek.mt6381eco.utils;

import com.mediatek.mt6381eco.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;

public class DataConverter {
  final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
  private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
  //private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  public static byte[] hexStringToByteArray(String s) {
    int len = s.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] =
          (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
    }
    return data;
  }

  public static int bytesToInt(byte[] bytes) {
    int result = 0;
    for (int i = bytes.length - 1; i >= 0; --i) {
      result = (result << 8) | (bytes[i] & 0xFF);
    }
    return result;
  }

  public static String bytesToHex(byte[] bytes) {
    char[] hexChars = new char[bytes.length * 3];
    for (int j = 0; j < bytes.length; j++) {
      int v = bytes[j] & 0xFF;
      hexChars[j * 3] = hexArray[v >>> 4];
      hexChars[j * 3 + 1] = hexArray[v & 0x0F];
      hexChars[j * 3 + 2] = ',';
    }
    return new String(hexChars);
  }

  public static String inputStreamToHex(InputStream inputStream) {
    try {
      inputStream.reset();
      if (inputStream.available() > 1000) {
        return "too large";
      }
      char[] hexChars = new char[inputStream.available() * 3];
      for (int j = 0; j < hexChars.length / 3; j++) {
        int v = inputStream.read();
        hexChars[j * 3] = hexArray[v >>> 4];
        hexChars[j * 3 + 1] = hexArray[v & 0x0F];
        hexChars[j * 3 + 2] = ',';
      }
      return new String(hexChars);
    } catch (IOException e) {
      e.printStackTrace();
      return e.toString();
    }
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

  public static int[] convertIntegers(List<Integer> integers) {
    int[] ret = new int[integers.size()];
    for (int i = 0; i < ret.length; i++) {
      ret[i] = integers.get(i).intValue();
    }
    return ret;
  }

  public static byte[] intToBytes(int i) {
    byte[] bytes = new byte[4];
    bytes[0] = (byte) (i & 0xFF);
    bytes[1] = (byte) ((i >> 8) & 0xFF);
    bytes[2] = (byte) ((i >> 16) & 0xFF);
    bytes[3] = (byte) ((i >> 24) & 0xFF);
    return bytes;
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

  public static StringBuilder intArrayToString(int[] ints, int startIndex, int endIndex) {
    StringBuilder stringBuilder = new StringBuilder();
    if (ints.length > startIndex) {
      stringBuilder.append(ints[startIndex]);

      for (int i = startIndex + 1; i < endIndex + 1; ++i) {
        stringBuilder.append(",");
        stringBuilder.append(ints[i]);
      }
    }
    return stringBuilder;
  }

  public static byte[] integersToBytes(List<Integer> integers) {
    return integersToBytes(convertIntegers(integers));
  }

  public static byte[] integersToBytes(int[] values) {

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    try {
      for (int i = 0; i < values.length; i++) {
        byteArrayOutputStream.write(DataConverter.intToBytes(values[i]));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    byte[] commandBytes = byteArrayOutputStream.toByteArray();

    return commandBytes;
  }

  public static String str2HexStr(String str) {

    char[] chars = "0123456789ABCDEF".toCharArray();
    StringBuilder sb = new StringBuilder();
    byte[] bs = str.getBytes();
    int bit;

    for (int i = 0; i < bs.length; i++) {
      bit = (bs[i] & 0x0f0) >> 4;
      sb.append(chars[bit]);
      bit = bs[i] & 0x0f;
      sb.append(chars[bit]);
    }
    return sb.toString().trim();
  }

  //public static String formatTimestamp(int timestamp) {
  //  return simpleDateFormat.format(new Date(timestamp * 1000L));
  //}

  public static int[] str2IntArray(String txt) {
    String[] str = txt.split(",");
    int[] array = new int[str.length];
    for (int i = 0; i < str.length; i++) {
      //array[i] = Integer.parseInt(str[i]);
      array[i] = (int) Float.parseFloat(str[i]);
    }
    return array;
  }

  public static long[] str2LongArray(String txt) {
    String[] str = txt.split(",");
    long[] array = new long[str.length];
    for (int i = 0; i < str.length; i++) {
      if (i == str.length - 1) {
        array[i] = 12345;
      } else {
        array[i] = Long.parseLong(str[i]);
      }
    }
    return array;
  }

  public static int calcHeight(int height, String heightUnit) {
    switch (heightUnit) {
      case "inch":
       return  (int) (height * 0.3937008f);
    }
    return height;
  }

  public static int calcWeight(int weight, String weightUnit) {
    switch (weightUnit) {
      case "jin":
        return weight / 2;
      case "lb":
        return (int) (weight * 0.4535924f);
    }
    return weight;
  }
}
