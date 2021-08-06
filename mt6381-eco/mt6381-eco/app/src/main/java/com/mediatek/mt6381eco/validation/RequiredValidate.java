package com.mediatek.mt6381eco.validation;

import android.content.Context;
import android.text.TextUtils;
import com.mediatek.mt6381eco.R;

public class RequiredValidate extends Validate {
  public RequiredValidate(Context context) {
    super(context.getString(R.string.validate_required_invalid));
  }

  @Override protected boolean isValid(String value) {
    return !TextUtils.isEmpty(value);
  }
}
