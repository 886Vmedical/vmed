package com.mediatek.mt6381eco.validation;

import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewParent;
import android.widget.EditText;
import com.mediatek.mt6381eco.rxbus.RxBus;
import java.util.ArrayList;

public class ViewValidation {
  private static final int DEFAULT_TAG = 0;
  private final EditText mEditText;
  private final ArrayList<Validate> validates = new ArrayList<>();
  private String errorMessage;
  private int tag = DEFAULT_TAG;
  private String mLastErrorMessage = errorMessage;

  public ViewValidation(EditText editText) {
    this.mEditText = editText;
    bindView(editText);
  }

  private void bindView(EditText editText) {
    editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
      @Override public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
          doValid();
        }
      }
    });
    editText.addTextChangedListener(new TextWatcher() {
      @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override public void onTextChanged(CharSequence s, int start, int before, int count) {

      }

      @Override public void afterTextChanged(Editable s) {
        doValid();
      }
    });
  }

  public ViewValidation addValid(Validate validate) {
    validates.add(validate);
    return this;
  }

  public boolean isValid() {
    errorMessage = "";
    String value = mEditText.getText().toString();
    for (Validate validate : validates) {
      if (!validate.isValid(value)) {
        errorMessage = validate.getErrorMessage();
        return false;
      }
    }
    return true;
  }

  private TextInputLayout getTextInputLayout(View view) {
    ViewParent parent = view.getParent();
    do {
      if (parent instanceof TextInputLayout) {
        return (TextInputLayout) parent;
      } else {
        parent = parent.getParent();
      }
    } while (parent != null);
    return null;
  }

  private void doValid() {
    TextInputLayout textInputLayout = getTextInputLayout(mEditText);
    boolean bIsValid = isValid();
    if (!bIsValid) {
      if (!errorMessage.equals(mLastErrorMessage)) {
        textInputLayout.setError(errorMessage);
        mLastErrorMessage = errorMessage;
      }
    } else {
      mLastErrorMessage = "";
      textInputLayout.setErrorEnabled(false);
    }
    RxBus.getInstance().post(new ValidateEvent(tag, bIsValid));
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setTag(int tag) {
    this.tag = tag;
  }
}
