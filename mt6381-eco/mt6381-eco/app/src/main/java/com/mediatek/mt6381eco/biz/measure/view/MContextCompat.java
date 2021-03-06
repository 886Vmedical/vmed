package com.mediatek.mt6381eco.biz.measure.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;

public class MContextCompat {
  public static final int getColor(Context context, int id) {
    return context.getResources().getColor(id);
  }

  public static final Drawable getDrawable(Context context, int id) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      return context.getDrawable(id);
    } else {
      return context.getResources().getDrawable(id);
    }
  }
}
