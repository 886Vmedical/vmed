package com.mediatek.mt6381eco.biz.utlis;

import org.junit.Test;

import static org.junit.Assert.*;

public class MSensorDataAlignerTest {
  @Test public void align() throws Exception {
    SensorDataAligner mSensorDataAligner =new SensorDataAligner();
    mSensorDataAligner.init(new int[]{1,2,3});
    int[] ret = mSensorDataAligner.align(1, 0, 0);
    assertNull(ret);
    ret = mSensorDataAligner.align(1, 1, 1);
    assertNull(ret);
    ret = mSensorDataAligner.align(1, 2, 2);
    assertNull(ret);


    ret = mSensorDataAligner.align(2, 0, 0);
    ret = mSensorDataAligner.align(3, 0, 0);
    assertArrayEquals(ret, new int[]{0,0,0});

    ret = mSensorDataAligner.align(3, 1, 1);
    ret = mSensorDataAligner.align(2, 1, 1);
    assertArrayEquals(ret, new int[]{1,1,1});


    ret = mSensorDataAligner.align(3, 2, 2);
    ret = mSensorDataAligner.align(2, 2, 2);
    assertArrayEquals(ret, new int[]{2,2,2});

    ret = mSensorDataAligner.align(3, 3, 3);
    ret = mSensorDataAligner.align(2, 3, 3);
    ret = mSensorDataAligner.align(1, 3, 3);
    assertArrayEquals(ret, new int[]{3,3,3});
  }

  @Test public void align2() throws Exception {
    SensorDataAligner mSensorDataAligner =new SensorDataAligner();
    mSensorDataAligner.init(new int[]{1,2,3});
    int[] ret = mSensorDataAligner.align(1, 0, 0);
    assertNull(ret);
    ret = mSensorDataAligner.align(1, 1, 1);
    assertNull(ret);
    ret = mSensorDataAligner.align(1, 2, 2);
    assertNull(ret);

    ret = mSensorDataAligner.align(1, 4, 2);
    assertNull(ret);

    ret = mSensorDataAligner.align(2, 0, 0);
    ret = mSensorDataAligner.align(3, 0, 0);
    assertNull(ret);


  }
}