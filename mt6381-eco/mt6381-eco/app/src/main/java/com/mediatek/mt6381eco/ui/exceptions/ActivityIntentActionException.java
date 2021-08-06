package com.mediatek.mt6381eco.ui.exceptions;

import android.content.Intent;
import lombok.Getter;

@Getter
public class ActivityIntentActionException extends Exception {

  private final Intent intent;
  public ActivityIntentActionException(Intent intent) {
    super(intent.getAction());
    this.intent = intent;
  }
}
