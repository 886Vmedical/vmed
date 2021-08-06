package com.mediatek.mt6381eco.biz.profile;

import lombok.Getter;

@Getter public class ValueUnit {
  private final int value;
  private final String unit;

  public ValueUnit(int value, String unit) {
    this.value = value;
    this.unit = unit;
  }
}
