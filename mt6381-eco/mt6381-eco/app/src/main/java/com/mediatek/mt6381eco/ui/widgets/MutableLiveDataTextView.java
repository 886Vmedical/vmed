package com.mediatek.mt6381eco.ui.widgets;

import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import com.mediatek.mt6381eco.utils.JsonUtils;
import timber.log.Timber;

public class MutableLiveDataTextView extends android.support.v7.widget.AppCompatTextView {

  private MutableLiveData mLiveData;

  public MutableLiveDataTextView(Context context) {
    super(context);
  }

  public MutableLiveDataTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public MutableLiveDataTextView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public <T> void setLiveData(LifecycleRegistryOwner owner, MutableLiveData<T> liveData,
      ToText<T> toText) {
    mLiveData = liveData;
    liveData.observe(owner, data -> setText(toText.toText(data)));
  }

  @Override public Parcelable onSaveInstanceState() {
    Parcelable superState = super.onSaveInstanceState();
    if (mLiveData != null && mLiveData.getValue() != null) {
      SavedState ss = new SavedState(superState);
      ss.clsName = mLiveData.getValue().getClass().getName();
      ss.json = JsonUtils.toJson(mLiveData.getValue());
      return ss;
    }
    return superState;
  }

  @SuppressWarnings("unchecked") @Override public void onRestoreInstanceState(Parcelable state) {
    if (!(state instanceof SavedState)) {
      super.onRestoreInstanceState(state);
      return;
    }
    SavedState ss = (SavedState) state;
    super.onRestoreInstanceState(ss.getSuperState());
    try {
      Object value = JsonUtils.fromJson(ss.json, ss.clsName);
      mLiveData.setValue(value);
    } catch (ClassNotFoundException e) {
      Timber.w(e);
    }
  }

  public interface ToText<T> {
    String toText(T data);
  }

  public static class SavedState extends BaseSavedState {
    public static final Parcelable.Creator<SavedState> CREATOR =
        new ClassLoaderCreator<SavedState>() {

          @Override public SavedState createFromParcel(Parcel in) {
            return new SavedState(in);
          }

          @Override public SavedState[] newArray(int size) {
            return new SavedState[size];
          }

          @Override public SavedState createFromParcel(Parcel in, ClassLoader classLoader) {
            return new SavedState(in);
          }
        };
    private String clsName;
    private String json;

    public SavedState(Parcelable superState) {
      super(superState);
    }

    SavedState(Parcel in) {
      super(in);
      clsName = in.readString();
      json = in.readString();
    }

    @Override public void writeToParcel(Parcel out, int flags) {
      super.writeToParcel(out, flags);
      out.writeString(clsName);
      out.writeString(json);
    }

    @Override public String toString() {
      return "InteractiveLineGraphView.SavedState{" + Integer.toHexString(
          System.identityHashCode(this)) + " json=" + json + "}";
    }
  }
}
