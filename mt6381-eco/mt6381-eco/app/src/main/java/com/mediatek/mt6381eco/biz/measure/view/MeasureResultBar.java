package com.mediatek.mt6381eco.biz.measure.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import com.mediatek.mt6381eco.R;
import com.mediatek.mt6381eco.ui.utils.UIUtils;
import com.mediatek.mt6381eco.utils.DrawUtils;
import lombok.Setter;

public class MeasureResultBar extends LinearLayout {
  private static final int LABEL_TEXT_SIZE = 12;
  private static final int BAR_HEIGHT = 6;
  private static final int MARKER_HEIGHT_HALF = 6;
  private static final int MARKER_WIDTH = 3;
  private static final float AXIS_SCALE_MARGIN_TOP = 8;
  private static final float AXIS_MARGIN_LEFT = 16;
  private static final float AXIS_MARGIN_BOTTOM = 56;
  private float mAxisMarginLeft;
  private float mAxisMarginBottom;
  private float mAxisScaleMarginTop;
  private int[] mBarColor = new int[3];
  private int[] mBarTitle = new int[3];
  private int[] mAxisXLabels = new int[4];
  private Context mContext;
  private TextPaint mPaintText;
  private Canvas mCanvas;
  private float mBarWidth;
  @Setter private Integer value;

  public MeasureResultBar(Context context) {
    this(context, null, 0);
  }

  public MeasureResultBar(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public MeasureResultBar(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    final TypedArray array = context.getTheme()
        .obtainStyledAttributes(attrs, R.styleable.MeasureResultBar, defStyleAttr, defStyleAttr);
    try {
      mBarColor =
          getResAttrsArray(context, array, R.styleable.MeasureResultBar_barColors, mBarColor);
      mBarTitle =
          getResAttrsArray(context, array, R.styleable.MeasureResultBar_barTitles, mBarTitle);
      mAxisXLabels = getAttrsArray(array, R.styleable.MeasureResultBar_axisXLabels, mAxisXLabels);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      array.recycle();
    }

    initViews(context);
  }

  private int[] getResAttrsArray(Context context, TypedArray array, int index, int[] attrsArr) {
    int resId = array.getResourceId(index, 0);
    if (resId != 0) {
      final TypedArray resArray = context.getResources().obtainTypedArray(resId);
      for (int i = 0; i < resArray.length(); i++) {
        final int resourceId = resArray.getResourceId(i, 0);
        attrsArr[i] = resourceId;
      }
      resArray.recycle();
    }
    return attrsArr;
  }

  private int[] getAttrsArray(TypedArray array, int index, int[] attrsArr) {
    final int resId = array.getResourceId(index, 0);
    if (resId != 0) {
      attrsArr = getResources().getIntArray(resId);
    }
    return attrsArr;
  }

  private void initViews(Context context) {
    mContext = context;
    inflate(getContext(), R.layout.view_measure_result_bar, this);
    setWillNotDraw(false);

    mPaintText =
        new TextPaint(R.color.eb_gray, UIUtils.dpToPx(LABEL_TEXT_SIZE, mContext), mContext);
  }

  @Override public void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    this.mCanvas = canvas;
    mAxisMarginLeft = UIUtils.dpToPx(AXIS_MARGIN_LEFT, mContext);
    mAxisMarginBottom = UIUtils.dpToPx(AXIS_MARGIN_BOTTOM, mContext);
    mAxisScaleMarginTop = UIUtils.dpToPx(AXIS_SCALE_MARGIN_TOP, mContext);
    mBarWidth = (canvas.getWidth() - mAxisMarginLeft * 2);

    setSubBar();

    setSubBarTitle();

    setAxisText();

    setMarkerOnAxis(value);
  }

  //setSubBar
  private void setSubBar() {
    for (int i = 0; i < mAxisXLabels.length - 1; i++) {
      paintSubAxis(ContextCompat.getColor(getContext(), mBarColor[i]),
          mAxisMarginLeft + getPointXInner(i), getSubBarWidth(i));
    }
  }

  private void paintSubAxis(int color, float position_x, float width) {
    float position_y = mCanvas.getHeight() - mAxisMarginBottom;
    AxisPaint paint = new AxisPaint(R.color.white, UIUtils.dpToPx(BAR_HEIGHT, mContext), mContext);
    paint.setColor(color);
    mCanvas.drawLine(position_x, position_y, position_x + width, position_y, paint);
  }

  //setSubBarTitle
  private void setSubBarTitle() {
    float position_y = mAxisScaleMarginTop;
    for (int i = 0; i < mBarTitle.length; i++) {
      String text = mContext.getResources().getString(mBarTitle[i]);
      float x = mAxisMarginLeft + getPointXInner(i) + getSubBarWidth(i) / 2
          - DrawUtils.getTextWidth(text, mPaintText) / 2;
      float y = position_y + DrawUtils.getTextHeight(text, mPaintText);
      mCanvas.drawText(text, x, y, mPaintText);
    }
  }

  //setAxisText
  private void setAxisText() {
    float position_y = mCanvas.getHeight() - mAxisMarginBottom + mAxisScaleMarginTop;
    for (int i = 0; i < mAxisXLabels.length; i++) {
      String text = String.valueOf(mAxisXLabels[i]);
      float x = mAxisMarginLeft + getPointXInner(i) - DrawUtils.getTextWidth(text, mPaintText) / 2;
      float y = position_y + DrawUtils.getTextHeight(text, mPaintText);
      mCanvas.drawText(text, x, y, mPaintText);
    }
  }

  //setMarkerOnAxis
  private void setMarkerOnAxis(Integer heartRate) {
    if (heartRate == null) {
      return;
    }

    float position_y = mCanvas.getHeight() - mAxisMarginBottom;
    float position_x = mAxisMarginLeft
        + ((float) (heartRate - mAxisXLabels[0])) / ((float) (mAxisXLabels[mAxisXLabels.length - 1]
        - mAxisXLabels[0])) * mBarWidth;

    ColorPaint paint = new ColorPaint(R.color.mtk_grey, mContext);
    paint.setStrokeWidth(UIUtils.dpToPx(MARKER_WIDTH, mContext));
    mCanvas.drawLine(position_x, position_y + UIUtils.dpToPx(MARKER_HEIGHT_HALF, mContext),
        position_x, position_y - UIUtils.dpToPx(MARKER_HEIGHT_HALF, mContext), paint);
  }

  private float getSubBarWidth(int i) {
    float curr = mAxisXLabels[i + 1] - mAxisXLabels[i];
    float total = mAxisXLabels[mAxisXLabels.length - 1] - mAxisXLabels[0];
    return (curr / total) * mBarWidth;
  }

  private float getPointXInner(int i) {
    float curr = mAxisXLabels[i] - mAxisXLabels[0];
    float total = mAxisXLabels[mAxisXLabels.length - 1] - mAxisXLabels[0];
    return (curr / total) * mBarWidth;
  }
}