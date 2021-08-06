package com.mediatek.mt6381eco.biz.measure.view;

import android.content.Context;
import android.support.annotation.ColorRes;

public class AxisPaint extends ColorPaint {

  public AxisPaint(@ColorRes int colorResId, float strokeWidth, Context context) {
    this(colorResId, strokeWidth, Alpha.OPAQUE, context);
  }

  public AxisPaint(@ColorRes int colorResId, float strokeWidth, int alpha, Context context) {
    super(colorResId, alpha, context);
    setStrokeWidth(strokeWidth);
    setAlpha(alpha);
  }
}
