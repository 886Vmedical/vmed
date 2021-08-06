package com.mediatek.mt6381eco.biz.measure;

import org.junit.Test;

import static org.junit.Assert.*;

public class SignalCheckerTest {
  @Test public void isGoodSignal() throws Exception {
   baseCheck(SignalChecker.SIGNAL_FINGER_EKG);
    baseCheck(SignalChecker.SIGNAL_FINGER_PPG);
    baseCheck(SignalChecker.SIGNAL_QUALITY_EKG);
    baseCheck(SignalChecker.SIGNAL_QUALITY_PPG1);
    baseCheck(SignalChecker.SIGNAL_QUALITY_PPG2);
    baseCheck(SignalChecker.SIGNAL_QUALITY_EKG | SignalChecker.SIGNAL_FINGER_EKG);


  }

  private void baseCheck(final int type){
    SignalChecker signalChecker = new SignalChecker(status -> System.out.println(status));
    assertTrue(signalChecker.isGoodSignal(signalChecker.getStatus()));

    signalChecker.setStatus(type, false);
    assertFalse(signalChecker.isGoodSignal(signalChecker.getStatus()));
    signalChecker.toggleChecking(type);
    assertFalse(signalChecker.isChecking(type));
    assertTrue(signalChecker.isGoodSignal(signalChecker.getStatus()));

    signalChecker.toggleChecking(type);
    assertTrue(signalChecker.isChecking(type));
    assertFalse(signalChecker.isGoodSignal(signalChecker.getStatus()));
  }
}