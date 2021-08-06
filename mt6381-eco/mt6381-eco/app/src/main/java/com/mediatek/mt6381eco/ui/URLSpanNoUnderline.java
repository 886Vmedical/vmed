package com.mediatek.mt6381eco.ui;

import android.os.Parcel;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.URLSpan;
import android.widget.TextView;

public class URLSpanNoUnderline extends URLSpan {
  public static final Creator<URLSpanNoUnderline> CREATOR = new Creator<URLSpanNoUnderline>() {
    @Override public URLSpanNoUnderline createFromParcel(Parcel in) {
      return new URLSpanNoUnderline(in);
    }

    @Override public URLSpanNoUnderline[] newArray(int size) {
      return new URLSpanNoUnderline[size];
    }
  };

  public URLSpanNoUnderline(String url) {
    super(url);
  }

  protected URLSpanNoUnderline(Parcel in) {
    super(in);
  }

  public static void setTo(TextView textView) {
    Spannable s = new SpannableString(textView.getText());
    URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);
    for (URLSpan span : spans) {
      int start = s.getSpanStart(span);
      int end = s.getSpanEnd(span);
      s.removeSpan(span);
      span = new URLSpanNoUnderline(span.getURL());
      s.setSpan(span, start, end, 0);
    }
    textView.setText(s);
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    super.writeToParcel(dest, flags);
  }

  @Override public void updateDrawState(TextPaint ds) {
    super.updateDrawState(ds);
    ds.setUnderlineText(false);
  }
}
