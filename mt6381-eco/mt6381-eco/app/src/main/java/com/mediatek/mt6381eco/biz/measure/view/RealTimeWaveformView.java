package com.mediatek.mt6381eco.biz.measure.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.View;
import com.mediatek.mt6381eco.R;
import com.mediatek.mt6381eco.biz.peripheral.SensorData;
import com.mediatek.mt6381eco.utils.DataUtils;
import io.reactivex.disposables.CompositeDisposable;
import java.util.List;

public class RealTimeWaveformView extends View {
  private static final int HOT_INTERVAL = 30;
  private static final int COLD_INTERVAL = 400;
  private static final int[] FILTER_TYPES =
      new int[] { SensorData.DATA_TYPE_EKG, SensorData.DATA_TYPE_PPG1 };
  private int mLastCount = -1;
  private long mInterval = HOT_INTERVAL;
  private final SparseArray<WavefromDraw> mDraws = new SparseArray<>();
  private final CompositeDisposable mDisposables = new CompositeDisposable();
  private final Matrix matrix = new Matrix();

  public RealTimeWaveformView(Context context) {
    this(context, null);
  }

  public RealTimeWaveformView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public RealTimeWaveformView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public void initTypes(int[] types) {
    int[] showTypes = DataUtils.and(types, FILTER_TYPES);
    ForLoop:
    for (int type : showTypes) {
      int resColor;
      int resDraw;
      switch (type) {
        case SensorData.DATA_TYPE_EKG: {
          resColor = R.color.ecg_color;
          resDraw = R.drawable.img_ecg;
          break;
        }
        case SensorData.DATA_TYPE_PPG1: {
          resColor = R.color.ppg_color;
          resDraw = R.drawable.img_ppg;
          break;
        }
        default:
          continue ForLoop;
      }
      Paint defaultPaint = getDefaultPaint();
      Paint paint = new Paint(defaultPaint);
      paint.setColor(ContextCompat.getColor(getContext(), resColor));
      WavefromDraw draw =
          new WavefromDraw(MContextCompat.getDrawable(getContext(), resDraw), paint);
      mDraws.put(type, draw);
    }
  }

  @Override protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    int width = this.getMeasuredWidth();
    int height = this.getMeasuredHeight();
    int dheight = height / 2 / mDraws.size();
    for (int i = 0; i < mDraws.size(); ++i) {
      WavefromDraw draw = mDraws.valueAt(i);
      try {
        canvas.save();
        matrix.reset();
        matrix.postTranslate(0, dheight + i * 2 * dheight);
        canvas.concat(matrix);
        draw.draw(canvas);
      } finally {
        canvas.restore();
      }
    }
    //for performance
    int thisCount = getDataCount();

    if(mLastCount != thisCount ){
      mInterval = HOT_INTERVAL;
    }else {
      mInterval = Math.min( ++mInterval, COLD_INTERVAL);
    }
    postInvalidateDelayed(mInterval);
    mLastCount = thisCount;
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    mDisposables.clear();
  }

  @Override protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
    if (changed) {
      int width = right - left;
      int height = bottom - top;
      for (int i = 0; i < mDraws.size(); ++i) {
        WavefromDraw draw = mDraws.valueAt(i);
        draw.layout(width, height / mDraws.size());
        draw.layout(width, height / mDraws.size());
      }
    }
  }

  private Paint getDefaultPaint() {
    float _1dp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1,
        getContext().getResources().getDisplayMetrics());

    Paint paint = new Paint();
    paint.setStyle(Paint.Style.FILL_AND_STROKE);
    paint.setFilterBitmap(false);

    paint.setStrokeWidth(_1dp * 1.5f);               // set the size
    paint.setDither(true);                    // set the dither to true
    paint.setStyle(Paint.Style.STROKE);       // set to STOKE
    paint.setStrokeJoin(Paint.Join.ROUND);    // set the join to round you want
    //     paint.setStrokeCap(Paint.Cap.ROUND);      // set the paint cap to round too
    paint.setPathEffect(new CornerPathEffect(_1dp * 2f));   // set the path effect when they join.
    paint.setAntiAlias(true);

    return paint;
  }

  public void setData(int type, List<Float> data) {
    WavefromDraw draw = mDraws.get(type);
    if(draw != null) {
      draw.setData(data);
    }
  }

  private int getDataCount() {
    int count = 0;
    for (int i = 0; i < mDraws.size(); ++i) {
      count += mDraws.valueAt(i).getCount();
    }
    return count;
  }
}
