package com.mediatek.blenativewrapper.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MeasureSpeedTest {
  @Test public void emptyTest() throws Exception {
    MeasureSpeed measureSpeed = new MeasureSpeed(2000, 50);
    assertEquals(0, measureSpeed.bps());
  }

  @Test public void oneTest() throws Exception {
    MeasureSpeed measureSpeed = new MeasureSpeed(2000, 50);
    for (int i = 0; i < 10000; ++i) {
      measureSpeed.receive(1,0);
    }
    assertEquals(10000 * (1000 / (2000/50)), measureSpeed.bps(0));
  }

  @Test public void freeSample() throws Exception {
    MeasureSpeed measureSpeed = new MeasureSpeed(2000, 50);
    for (int i = 0; i < 2000; ++i) {
      measureSpeed.receive(i % 1000, i);
    }
    assertEquals(999 * 1000 / 2, measureSpeed.bps(1999));
    assertEquals(999 * 1000 / 2/2, measureSpeed.bps(2999));
    assertEquals(0, measureSpeed.bps(4000));
    for (int i = 4000; i < 6000; ++i) {
      measureSpeed.receive(1, i);
    }
    assertEquals(1000, measureSpeed.bps(5999));
  }
}