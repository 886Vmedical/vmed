package com.mediatek.mt6381eco.ui.data;

import android.content.Intent;
import lombok.Getter;

@Getter
public class IntentResult {
  private static final int EMPTY_RESULT = -1;
  private final int resultCode;
  private final Intent data;

  public IntentResult(int resultCode, Intent data) {
    this.resultCode = resultCode;
    this.data = data;
  }

  public static IntentResult empty() {
    return new IntentResult(EMPTY_RESULT, null);
  }
}
