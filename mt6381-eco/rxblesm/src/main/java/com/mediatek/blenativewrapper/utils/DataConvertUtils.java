package com.mediatek.blenativewrapper.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class DataConvertUtils {
  private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

  public static String bytesToHex(byte[] bytes) {
    if (bytes.length < 1) {
      return "";
    }
    char[] hexChars = new char[bytes.length * 3 - 1];
    for (int j = 0; j < bytes.length; j++) {
      int v = bytes[j] & 0xFF;
      hexChars[j * 3] = hexArray[v >>> 4];
      hexChars[j * 3 + 1] = hexArray[v & 0x0F];
      if (j != bytes.length - 1) {
        hexChars[j * 3 + 2] = '-';
      }
    }
    return new String(hexChars);
  }

  /**
   * @param charBuffer String buffer.
   * @param bytes source bytes.
   * @param startPos startIndex of source bytes
   * @return Returns the length of string chars.
   */

  public static int bytesToHex(char[] charBuffer, byte[] bytes, int startPos, int length) {
    if (length < 1) {
      return 0;
    }
    int i = 0;
    int setLength = Math.min((charBuffer.length + 1) / 3, length);
    int j;
    int sourceEnd = setLength + startPos;
    for (j = startPos; j < sourceEnd - 1; j++) {
      int v = bytes[j] & 0xFF;
      charBuffer[i++] = hexArray[v >>> 4];
      charBuffer[i++] = hexArray[v & 0x0F];
      charBuffer[i++] = '-';
    }
    if (j < sourceEnd) {
      int v = bytes[j] & 0xFF;
      charBuffer[i++] = hexArray[v >>> 4];
      charBuffer[i++] = hexArray[v & 0x0F];
    }
    if (j < startPos + length - 1) {
      charBuffer[i - 1] = '*';
    }
    return i;
  }

  public static byte[] readBytes(InputStream inputStream) throws IOException {
    ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
    int bufferSize = 512;
    byte[] buffer = new byte[bufferSize];
    int len;
    while ((len = inputStream.read(buffer)) != -1) {
      byteBuffer.write(buffer, 0, len);
    }
    return byteBuffer.toByteArray();
  }
}
