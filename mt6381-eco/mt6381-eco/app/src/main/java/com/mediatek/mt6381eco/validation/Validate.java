package com.mediatek.mt6381eco.validation;

public class Validate {
  public String getErrorMessage() {
    return errorMessage;
  }

  private final String errorMessage;

  public Validate(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  protected boolean isValid(String value) {
    return true;
  }


}