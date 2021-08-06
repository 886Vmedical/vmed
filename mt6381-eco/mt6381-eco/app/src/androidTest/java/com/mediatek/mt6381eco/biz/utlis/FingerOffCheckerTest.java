package com.mediatek.mt6381eco.biz.utlis;

import org.junit.Test;

import static org.junit.Assert.*;

public class FingerOffCheckerTest {
  @Test public void check() throws Exception {
    FingerOffChecker fingerOffChecker = new FingerOffChecker();
    for(int i =0;i < 33; ++i){
      assertTrue("checkFingerOff",fingerOffChecker.isFingerOff(0, 0) == i >=31);
    }

    assertTrue("checkFingerOff", !fingerOffChecker.isFingerOff(0, 1));
    for(int i =0;i < 33; ++i){
      assertTrue("checkFingerOff",fingerOffChecker.isFingerOff(0, 0) == i >=31);
    }
  }
}