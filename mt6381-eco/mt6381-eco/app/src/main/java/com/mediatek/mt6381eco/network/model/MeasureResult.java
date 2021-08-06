package com.mediatek.mt6381eco.network.model;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter public class MeasureResult {

  private int measurementId;
  private long timestamp;
  private Data systolic;
  private Data diastolic;
  private Data spo2;
  private Data heartRate;
  private Data brv;
  private Data fatigue;
  private Data pressure;

  @Getter @Setter public static class Data {
    private Integer value;
    private Integer status;
  }
}
