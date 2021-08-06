package com.mediatek.mt6381eco.biz.measure.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import com.mediatek.mt6381eco.R;
import com.mediatek.mt6381eco.ui.utils.UIUtils;
import timber.log.Timber;

public class CountdownPb extends LinearLayout {
  private static final int TYPE_ONE_COLOR = 0;
  private static final int TYPE_TWO_COLOR = 1;
  private static final int TYPE_THREE_COLOR = 2;
  final float RING_THICKNESS = 6;
  final float START_ANGLE = -225;
  public int startColorResId = R.color.mtk_orange;
  public int middleColorResId = R.color.mtk_pink;
  public int endColorResId = R.color.mtk_pink;
  public int bgColorResId = R.color.black_pressed;
  private final int radus_padding = 25;
  private final int RING_RADIUS = 53;
  private int progress = 100;
  private final LayoutInflater inflater;
  private ColorPaint p_arc;
  private ColorPaint p_dot;
  private Canvas canvas;
  private RectF rect;
  private LinearGradient gradient;
  private Context mContext;
  private int type = TYPE_THREE_COLOR;

  public CountdownPb(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
    inflater = LayoutInflater.from(context);
    inflater.inflate(R.layout.view_count_down_pb, this, true);

    setWillNotDraw(false);
  }

  public void setProgress(int progress) {
    if (this.progress != progress) {
      //Timber.d("progress:" + progress);
      this.progress = progress;
      invalidate();
    }
  }

  @Nullable @Override protected Parcelable onSaveInstanceState() {
    SavedState savedState = new SavedState(super.onSaveInstanceState());
    savedState.process = progress;
    return savedState;
  }

  @Override protected void onRestoreInstanceState(Parcelable state) {
    if(!(state instanceof SavedState)) {
      super.onRestoreInstanceState(state);
      return;
    }

    SavedState ss = (SavedState)state;
    super.onRestoreInstanceState(ss.getSuperState());
    this.progress = ss.process;
  }

  private void init(Context context, AttributeSet attrs) {
    if (attrs == null) {
      Timber.e("Attrs is null");
    } else {
      this.mContext = context;
      TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CustomView);
      startColorResId = array.getColor(R.styleable.CustomView_startColor, 0xFF000000);
      middleColorResId = array.getColor(R.styleable.CustomView_middleColor, 0xFF000000);
      endColorResId = array.getColor(R.styleable.CustomView_endColor, 0xFF000000);
      //mTextColor = array.getColor(R.styleable.CustomView_tColor, 0xFF000000);

      Timber.i("startColorResId:" + startColorResId);
      array.recycle();

      rect = new RectF();
      gradient = new LinearGradient(0, getHeight(), getWidth(), getHeight(), this.endColorResId,
          this.startColorResId, Shader.TileMode.MIRROR);
    }
  }

  public void setTwoColorGradient(int startColorResId, int endColorResId) {
    this.startColorResId = startColorResId;
    this.endColorResId = endColorResId;
    type = TYPE_TWO_COLOR;
    invalidate();
  }

  public void setThreeColorGradient(int startColorResId, int middleColorResId, int endColorResId) {
    this.startColorResId = startColorResId;
    this.middleColorResId = middleColorResId;
    this.endColorResId = endColorResId;
    type = TYPE_THREE_COLOR;
    invalidate();
  }

  @Override protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    //Timber.d("onDraw");
    //Timber.d("getMeasuredHeight(): %s", getMeasuredHeight());
    //float circle_radius = UIUtils.dpToPx(RING_RADIUS);

    this.canvas = canvas;
    setPaint();

    int center_x = canvas.getWidth() / 2;
    int center_y = canvas.getHeight() / 2;

    //Timber.d("center_y(): %s", center_y);
    float circle_radius = center_y - radus_padding;

    float left = center_x - circle_radius;
    float width = circle_radius * 2;
    float top = center_y - circle_radius;
    rect.set(left, top, left + width, top + width);

    switch (type) {
      case TYPE_TWO_COLOR: {
        gradient = new LinearGradient(0, getHeight(), getWidth(), getHeight(), this.endColorResId,
            this.startColorResId, Shader.TileMode.MIRROR);
        break;
      }
      case TYPE_THREE_COLOR: {
        gradient = new LinearGradient(0, getHeight(), getWidth(), getHeight(), new int[] {
            this.endColorResId, this.middleColorResId, this.startColorResId
        }, new float[] { 0, 0.5f, 1.0f }, Shader.TileMode.MIRROR);
        break;
      }
    }

    p_arc.setColor(ContextCompat.getColor(getContext(), this.bgColorResId));
    p_dot.setColor(ContextCompat.getColor(getContext(), this.bgColorResId));
    canvas.drawArc(rect, START_ANGLE, 270, false, p_arc);
    drawDot(canvas, START_ANGLE * Math.PI / 180);
    drawDot(canvas, (START_ANGLE + 270) * Math.PI / 180);

    p_arc.setShader(gradient);
    float sweepAngle = (360 - 90) * ((float) progress) / 100;
    //Timber.d("sweepAngle=" + sweepAngle);
    canvas.drawArc(rect, START_ANGLE, sweepAngle, false, p_arc);

    p_dot.setShader(gradient);
    double angle = ((360 - 90) * ((float) progress) / 100 + START_ANGLE) * Math.PI / 180;
    drawDot(canvas, angle);

    angle = START_ANGLE * Math.PI / 180;
    drawDot(canvas, angle);

    //invalidate();
  }

  private void drawDot(Canvas canvas, double angle) {
    int center_x = canvas.getWidth() / 2;
    int center_y = canvas.getHeight() / 2;
    //float circle_radius = UIUtils.dpToPx(RING_RADIUS);
    float circle_radius = center_y - radus_padding;

    canvas.drawCircle(center_x + circle_radius * (float) Math.cos(angle),
        center_y + circle_radius * (float) Math.sin(angle),
        UIUtils.dpToPx(RING_THICKNESS, mContext) / 2, p_dot);
  }

  private void setPaint() {
    p_arc = new ColorPaint(R.color.mtk_grey, mContext);
    p_arc.setStyle(Paint.Style.STROKE);
    p_arc.setStrokeWidth(UIUtils.dpToPx(RING_THICKNESS, mContext));

    p_dot = new ColorPaint(R.color.mtk_grey, mContext);
    p_dot.setStrokeWidth(UIUtils.dpToPx(RING_THICKNESS, mContext));
    p_dot.setStyle(Paint.Style.FILL);
  }
  static class SavedState extends BaseSavedState {
    private int process;
    @Override public int describeContents() {
      return 0;
    }
    SavedState(Parcelable superState) {
      super(superState);
    }
    @Override
    public void writeToParcel(Parcel out, int flags) {
      super.writeToParcel(out, flags);
      out.writeInt(this.process);
    }
    private SavedState(Parcel in) {
      super(in);
      this.process = in.readInt();
    }


    public static final Parcelable.Creator<SavedState> CREATOR =
        new Parcelable.Creator<SavedState>() {
          @Override public SavedState createFromParcel(Parcel source) {
            return new SavedState(source);
          }

          @Override public SavedState[] newArray(int size) {
            return new SavedState[size];
          }
        };
  }
}

