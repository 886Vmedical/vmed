package com.mediatek.mt6381eco.ui;

import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.MutableLiveData;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import com.mediatek.mt6381eco.R;
import com.mediatek.mt6381eco.ui.exceptions.UIBindParseException;
import com.mediatek.mt6381eco.utils.MTextUtils;
import com.mediatek.mt6381eco.ui.utils.UIUtils;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
import lombok.AllArgsConstructor;

public class UiBinder {
  private static final DataAdapter<String> DATA_ADAPTER_STRING = new StringDataAdapter();
  private static final DataAdapter<Date> DATA_ADAPTER_DATE = new DateDataAdapter();
  private static final DataAdapter<Integer> DATA_ADAPTER_INTEGER = new IntegerDataAdapter();
  private static final DataAdapter<Float> DATA_ADAPTER_FLOAT = new FloatDataAdapter();

  private final LifecycleRegistryOwner owner;
  private final LinkedList<TextViewDataPackage> mBindList = new LinkedList<>();

  public UiBinder(LifecycleRegistryOwner owner) {
    this.owner = owner;
  }

  public void bindString(TextView textView, MutableLiveData<String> liveData) {
    bind(textView, liveData, DATA_ADAPTER_STRING);
  }

  public void bindInteger(TextView textView, MutableLiveData<Integer> liveData) {
    bind(textView, liveData, DATA_ADAPTER_INTEGER);
  }

  public void bindFloat(TextView textView, MutableLiveData<Float> liveData) {
    bind(textView, liveData, DATA_ADAPTER_FLOAT);
  }

  public void bindDate(TextView textView, MutableLiveData<Date> liveData) {
    bind(textView, liveData, DATA_ADAPTER_DATE);
  }

  public void postUiChange() throws UIBindParseException {
    for (TextViewDataPackage item : mBindList) {
      try {
        Object value = item.dataAdapter.toData(item.textView.getText().toString());
        item.liveData.setValue(value);
      } catch (Exception e) {
        TextView labelView =
            UIUtils.findLabelFor(item.textView.getId(), (ViewGroup) item.textView.getParent());
        String message = item.textView.getContext().getString(R.string.invalid_data_format);
        if (labelView != null) {
          message = labelView.getText() + ":" + message;
        }
        throw new UIBindParseException(message, e.getCause(), item.textView);
      }
    }
  }

  public <T> void bind(TextView textView, MutableLiveData<T> liveData, DataAdapter<T> dataAdapter) {
    liveData.observe(owner, data -> {
      String text = dataAdapter.toText(data);
      if (!textView.getText().toString().equals(text)) {
        textView.setText(text);
        if(textView instanceof EditText) {
          ((EditText)textView).setSelection(text.length());
        }
        textView.requestFocus();
      }
    });
    mBindList.add(new TextViewDataPackage<>(textView, liveData, dataAdapter));
  }

  public interface DataAdapter<T> {
    String toText(T data);

    T toData(String text) throws Exception;
  }

  @AllArgsConstructor private static class TextViewDataPackage<T> {
    private TextView textView;
    private MutableLiveData<T> liveData;
    private DataAdapter<T> dataAdapter;
  }

  private static class StringDataAdapter implements DataAdapter<String> {

    @Override public String toText(String data) {
      if (null == data) return "";
      return data;
    }

    @Override public String toData(String text) throws Exception {
      return text;
    }
  }

  private static class IntegerDataAdapter implements DataAdapter<Integer> {
    @Override public String toText(Integer data) {
      if (null == data) return "";
      return String.valueOf(data);
    }

    @Override public Integer toData(String text) throws Exception {
      if(TextUtils.isEmpty(text)) return null;
      return Integer.valueOf(text);
    }
  }

  private static class FloatDataAdapter implements DataAdapter<Float> {
    @Override public String toText(Float data) {
      if (null == data) return "";
      return MTextUtils.format(data);
    }

    @Override public Float toData(String text) throws Exception {
      if(TextUtils.isEmpty(text)) return null;
      return Float.valueOf(text);
    }
  }

  private static class DateDataAdapter implements DataAdapter<Date> {
    private static final SimpleDateFormat FORMATTER_DATA =
        new SimpleDateFormat("yyyy/MM/dd", Locale.US);

    @Override public String toText(Date data) {
      if (null == data) return "";
      return FORMATTER_DATA.format(data);
    }

    @Override public Date toData(String text) throws Exception {
      return FORMATTER_DATA.parse(text);
    }
  }
}
