package com.mediatek.mt6381eco.ui.exceptions;

import android.view.View;
import lombok.Getter;

public class UIBindParseException extends Exception {
  @Getter
  private final View view;

  public UIBindParseException(String message,Throwable cause, View view){
    super(message, cause);
    this.view = view;
  }
}
