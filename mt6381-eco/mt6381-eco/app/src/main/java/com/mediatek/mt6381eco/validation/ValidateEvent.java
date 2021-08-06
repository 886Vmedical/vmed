package com.mediatek.mt6381eco.validation;

public class ValidateEvent {
  private final int tag;
  private final boolean isValid;

  public ValidateEvent(int tag, boolean isValid){
    this.tag = tag;
    this.isValid = isValid;
  }

  public int getTag() {
    return tag;
  }

  public boolean isValid() {
    return isValid;
  }
}
