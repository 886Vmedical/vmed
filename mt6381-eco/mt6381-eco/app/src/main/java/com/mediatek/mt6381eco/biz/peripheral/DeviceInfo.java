package com.mediatek.mt6381eco.biz.peripheral;

import lombok.Getter;

@Getter public class DeviceInfo {
  private final int throughput;
  private final int battery;
  private final String firmwareVersion;
  public DeviceInfo(int throughput, int battery, String firmwareVersion) {
    this.throughput = throughput;
    this.battery = battery;
    this.firmwareVersion = firmwareVersion;
    //this.firmwareVersion = "3A7A";
  }
}
