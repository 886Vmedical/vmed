package com.mediatek.mt6381eco.biz.measure;

import org.junit.Test;

import static org.junit.Assert.*;

public class IntArrayVersionTest {
  @Test public void isChanged() throws Exception {
    IntArrayVersion intArrayVersion = new IntArrayVersion();
    assertTrue(intArrayVersion.isChanged());
    assertTrue(intArrayVersion.isChanged(1));
    assertTrue(!intArrayVersion.isChanged(1));
    assertTrue(intArrayVersion.isChanged(2));
  }
}