package com.mediatek.mt6381eco.network.model;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter public class TemperatureResult {

  private int temperatureId;
  private long timestamp;
  private float temperature;
}
