package com.mediatek.mt6381eco.ui.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextPaint;
import android.util.AttributeSet;

public class VerticalTextView extends AppCompatTextView {
  private final Rect mBounds = new Rect();
  private TextPaint mTextPaint;
  private int mColor;

  public VerticalTextView(Context context) {
    super(context);
  }

  public VerticalTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
    mColor = getCurrentTextColor();
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    mTextPaint = getPaint();
    mTextPaint.getTextBounds((String) getText(), 0, getText().length(), mBounds);
    setMeasuredDimension((int) (mBounds.height() + mTextPaint.descent()), mBounds.width());
  }

  @Override protected void onDraw(Canvas canvas) {
    mTextPaint.setColor(mColor);
    canvas.rotate(-90, mBounds.width(), 0);
    canvas.drawText((String) getText(), 0, -mBounds.width() + mBounds.height(), mTextPaint);
  }
}
