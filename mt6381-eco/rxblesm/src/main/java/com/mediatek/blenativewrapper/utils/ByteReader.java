package com.mediatek.blenativewrapper.utils;

public class ByteReader implements BufferReader{
  private final byte[] data;
  private int pos = 0;

  public ByteReader(byte[] data) {
    this.data = data;
  }

  @Override public byte[] read(int size) {
    int length = Math.min(data.length - pos, size);
    if (length <= 0) {
      return null;
    }
    byte[] ret = new byte[length];
    System.arraycopy(data, pos, ret, 0, length);
    pos += length;
    return ret;
  }
}
