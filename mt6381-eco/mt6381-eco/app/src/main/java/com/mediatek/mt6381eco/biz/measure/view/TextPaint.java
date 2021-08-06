package com.mediatek.mt6381eco.biz.measure.view;

import android.content.Context;
import android.support.annotation.ColorRes;

public class TextPaint extends ColorPaint {

  public TextPaint(@ColorRes int colorResId, float textSize, Context context) {
    this(colorResId, textSize, Alpha.OPAQUE, context);
  }

  public TextPaint(@ColorRes int colorResId, float textSize, int alpha, Context context) {
    super(colorResId, alpha, context);
    setTextSize(textSize);
  }
}
