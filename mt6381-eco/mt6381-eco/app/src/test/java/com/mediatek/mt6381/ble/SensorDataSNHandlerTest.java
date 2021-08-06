package com.mediatek.mt6381.ble;

import com.mediatek.mt6381eco.biz.peripheral.SensorData;
import org.junit.Test;

import static org.junit.Assert.*;

public class SensorDataSNHandlerTest {
  @Test public void testSensorDataSNHandler() throws Exception {
    SensorDataSNHandler sensorDataSNHandler = new SensorDataSNHandler();

    int index = 0;
    for(int j =0;j < 300;++j) {
      for (int i = 0; i < 256; ++i) {
        SensorData sensorData = new SensorData();
        sensorData.sn = i;

        sensorData = sensorDataSNHandler.handle(sensorData);

        assertEquals(index++,sensorData.sn);
      }
    }

  }

  @Test public void testSensorDataSNHandler2() throws Exception {
    SensorDataSNHandler sensorDataSNHandler = new SensorDataSNHandler();

    int index = 1;
    for(int j =0;j < 300;++j) {
      for (int i = 0; i < 256; ++i) {
        SensorData sensorData = new SensorData();
        sensorData.sn = i+1;

        sensorData = sensorDataSNHandler.handle(sensorData);

        assertEquals(index++,sensorData.sn);
      }
    }

  }
}