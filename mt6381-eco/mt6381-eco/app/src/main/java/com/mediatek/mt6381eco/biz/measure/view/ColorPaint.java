package com.mediatek.mt6381eco.biz.measure.view;

import android.content.Context;
import android.graphics.Paint;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;

public class ColorPaint extends Paint {

  public ColorPaint(@ColorRes int colorResId, Context context) {
    this(colorResId, Alpha.OPAQUE, context);
  }

  public ColorPaint(@ColorRes int colorResId, int alpha, Context context) {
    setColor(ContextCompat.getColor(context, colorResId));
    setStyle(Style.FILL);
    setAlpha(alpha);
    setAntiAlias(true);
  }
}
