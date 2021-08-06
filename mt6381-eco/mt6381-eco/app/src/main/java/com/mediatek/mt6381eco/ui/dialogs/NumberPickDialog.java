package com.mediatek.mt6381eco.ui.dialogs;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mediatek.mt6381eco.R;
import com.mediatek.mt6381eco.utils.MTextUtils;

public class NumberPickDialog extends MaterialDialog {
  protected NumberPickDialog(Builder builder) {
    super(builder.builderImpl);
    builder.pickerNumber.setMinValue(builder.minValue);
    builder.pickerNumber.setMaxValue(builder.maxValue);
    builder.pickerNumber.setValue(builder.value);
    builder.pickerUnit.setDisplayedValues(builder.units);
    builder.pickerUnit.setMinValue(0);
    builder.pickerUnit.setMaxValue(builder.units.length -1);
    int index = MTextUtils.indexOf(builder.units, builder.unit);
    if (index < 0 || index > builder.units.length - 1) {
      index = 0;
    }
    builder.txtUnit.setText(builder.unit);
    builder.pickerUnit.setValue(index);
    builder.txtUnit.setVisibility(builder.units.length <2 ? View.VISIBLE:View.GONE);
    builder.pickerUnit.setVisibility(builder.units.length >1 ? View.VISIBLE:View.GONE);

  }

  public interface Callback {
    void onResult(int value, String unit);
  }

  public static class Builder {
    protected final MaterialDialog.Builder builderImpl;
    private final Context context;
    protected int minValue = 0;
    protected int maxValue = 0;
    protected int value = 0;
    protected NumberPicker pickerNumber;
    protected NumberPicker pickerUnit;
    private String unit;
    private Callback callBack;
    private String[] units;
    private TextView txtUnit;

    public Builder(@NonNull Context context) {
      this.context = context;
      builderImpl = new MaterialDialog.Builder(context);
      init();
    }

    private void init() {
      final LayoutInflater inflater = LayoutInflater.from(context);
      View layoutNumberPicker = inflater.inflate(R.layout.layout_number_picker, null);
      pickerNumber = layoutNumberPicker.findViewById(R.id.picker_number);
      pickerUnit = layoutNumberPicker.findViewById(R.id.picker_unit);
      txtUnit = layoutNumberPicker.findViewById(R.id.txt_unit);
      builderImpl.customView(layoutNumberPicker, false);
      builderImpl.positiveText(R.string.ok);
      builderImpl.negativeText(R.string.cancel);
      builderImpl.onPositive((dialog, which) -> {
        if (callBack != null) {
          int value = pickerNumber.getValue();
          String unit = pickerUnit.getDisplayedValues()[pickerUnit.getValue()];
          callBack.onResult(value, unit);
        }
      });
    }

    public Builder minValue(int minValue) {
      this.minValue = minValue;
      return this;
    }

    public Builder maxValue(int maxValue) {
      this.maxValue = maxValue;
      return this;
    }

    public Builder value(int value) {
      this.value = value;
      return this;
    }

    public Builder units(String[] units) {
      this.units = units;
      return this;
    }

    public Builder unit(String unit) {
      this.unit = unit;
      return this;
    }

    public Builder callBack(Callback callback) {
      this.callBack = callback;
      return this;
    }

    public NumberPickDialog build() {
      return new NumberPickDialog(this);
    }

    public void show() {
      build().show();
    }

    public Builder title(@StringRes int titleRes) {
      builderImpl.title(titleRes);
      return this;
    }

    public Builder iconRes(@DrawableRes int icon) {
      builderImpl.iconRes(icon);
      return this;
    }
  }
}
