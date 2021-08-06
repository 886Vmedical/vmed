package com.mediatek.mt6381eco.validation;

import android.content.Context;
import com.mediatek.mt6381eco.R;

public class NumberValidate extends Validate {
  private final int maxValue;
  private final int minValue;

  public NumberValidate(Context context, int minValue, int maxValue) {
    super(context.getString(R.string.validate_number_range_invalid, minValue, maxValue));
    this.minValue = minValue;
    this.maxValue = maxValue;
  }

  @Override protected boolean isValid(String value) {
    try {
      int intValue = Integer.parseInt(value);
        return intValue >= minValue && intValue <= maxValue;
    } catch (NumberFormatException e) {
      return false;
    }
  }
}
