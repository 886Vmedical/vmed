package com.mediatek.mt6381eco.ui.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.widget.TextView;
import com.mediatek.mt6381eco.R;

public class MToolbar extends Toolbar {

  private TextView mTitleTextView;
  private boolean mTitleCenter;

  public MToolbar(Context context) {
    super(context);
    init(context);
  }

  public MToolbar(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  public MToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context);
  }

  @Override public void setTitle(CharSequence title) {
    super.setTitle(title);
    if(mTitleCenter && getChildAt(0) instanceof TextView){
      mTitleTextView = (TextView) getChildAt(0);
    }

  }
  private void init(Context context){
    int[] tempAttrs = { R.attr.MTitleCenter};
    TypedArray ta =context.obtainStyledAttributes(tempAttrs);
    mTitleCenter = ta.getBoolean(0, false);
    ta.recycle();
  }
  @Override protected void onLayout(boolean changed, int l, int t, int r, int b) {
    super.onLayout(changed, l, t, r, b);
    final int width = getWidth();
    final int height = getHeight();


    if(mTitleCenter && mTitleTextView != null){
      final int titleWidth = mTitleTextView.getMeasuredWidth();
      final int titleHeight = mTitleTextView.getMeasuredHeight();
      final int titleLeft =  (width - titleWidth) /2;
      final int titleTop =  (height - titleHeight) /2;
      final int titleBottom = titleTop + titleHeight;
      final int titleRight = titleLeft + titleWidth;
      mTitleTextView.layout(titleLeft, titleTop, titleRight, titleBottom);
    }

  }
}
