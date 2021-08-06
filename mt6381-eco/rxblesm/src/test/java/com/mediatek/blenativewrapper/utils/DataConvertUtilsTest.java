package com.mediatek.blenativewrapper.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class DataConvertUtilsTest {
  @Test public void bytesToHex() throws Exception {
    char[] buffer = new char[8];
    int len = DataConvertUtils.bytesToHex(buffer, new byte[]{ (byte) 0xfa,0x34,0x12}, 0, 2);
    assertEquals("FA-34", new String(buffer, 0, len));



    buffer = new char[7];
    len = DataConvertUtils.bytesToHex(buffer, new byte[]{ (byte) 0xfa,0x34,0x12}, 0, 3);
    assertEquals("FA-3*", new String(buffer, 0, len));
  }
}