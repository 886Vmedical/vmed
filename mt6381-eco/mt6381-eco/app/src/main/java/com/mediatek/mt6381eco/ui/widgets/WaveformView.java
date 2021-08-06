package com.mediatek.mt6381eco.ui.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.EdgeEffect;
import android.widget.OverScroller;
import com.mediatek.mt6381eco.R;

import java.util.ArrayList;

public class WaveformView extends View {

  private static final String TAG = "WaveGraphView";

  /**
   * Initial fling velocity for pan operations, in screen widths (or heights) per second.
   *
   * @see #panLeft()
   * @see #panRight()
   * @see #panUp()
   * @see #panDown()
   */
  private static final float PAN_VELOCITY_FACTOR = 2f;

  /**
   * The scaling factor for a single zoom 'step'.
   *
   * @see #zoomIn()
   * @see #zoomOut()
   */
  private static final float ZOOM_AMOUNT = 0.25f;
  private static final float ZOOM_MIN = 0.2f;
  private static final float ZOOM_MAX = 1f;
  // Viewport extremes. See mCurrentViewport for a discussion of the viewport.
  private static final int DATA_COUNT = 500;
  private static final float AXIS_X_MIN = 0f;


  private static final float  POINTS_PRE_MM = 512f /4f / 25f;
  private final float m1MMPx;

  /**
   * The current viewport. This rectangle represents the currently visible chart domain
   * and range. The currently visible chart X values are from this rectangle's left to its right.
   * The currently visible chart Y values are from this rectangle's top to its bottom.
   * <p>
   * Note that this rectangle's top is actually the smaller Y value, and its bottom is the larger
   * Y value. Since the chart is drawn onscreen in such a way that chart Y values increase
   * towards the top of the screen (decreasing pixel Y positions), this rectangle's "top" is drawn
   * above this rectangle's "bottom" value.
   *
   * @see #mContentRect
   */
  private RectF mCurrentViewport = new RectF();
  /**
   * The current destination rectangle (in pixel coordinates) into which the chart mData should
   * be drawn. Chart labels are drawn outside this area.
   *
   * @see #mCurrentViewport
   */
  private final Rect mContentRect = new Rect();
  private float mGrid1Thickness = 1f;
  private float mGrid2Thickness = 1f;
  private int mGridColor;
  private Paint mGridPaint1;
  private Paint mGridPaint2;
  private float mDataThickness;
  // State objects and values related to gesture tracking.
  private final ScaleGestureDetector mScaleGestureDetector;
  private final GestureDetectorCompat mGestureDetector;
  private final OverScroller mScroller;
  private final Zoomer mZoomer;
  private final PointF mZoomFocalPoint = new PointF();
  private final RectF mScrollerStartViewport = new RectF(); // Used only for zooms and flings.
  // Edge effect / overscroll tracking objects.
  private final EdgeEffect mEdgeEffectTop;
  private final EdgeEffect mEdgeEffectBottom;
  private final EdgeEffect mEdgeEffectLeft;
  private final EdgeEffect mEdgeEffectRight;

  private boolean mEdgeEffectTopActive;
  private boolean mEdgeEffectBottomActive;
  private boolean mEdgeEffectLeftActive;
  private boolean mEdgeEffectRightActive;
  private final float[] mDrawBuffer = new float[8000];
  private final Point mSurfaceSizeBuffer = new Point();
  private  float mXMax = DATA_COUNT;
  private  float mYMin = -200f;
  private  float mYMax = 200f;

  private final ArrayList<DataInfo> mData = new ArrayList<>();


  private OnScrollListener mOnScrollListener;
  /**
   * The gesture listener, used for handling simple gestures such as double touches, scrolls,
   * and flings.
   */
  private final GestureDetector.SimpleOnGestureListener mGestureListener =
      new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

          if(!isVerticalScrollBarEnabled()) {
            distanceY = 0;
          }
          float viewportOffsetX = distanceX * mCurrentViewport.width() / mContentRect.width();
          float viewportOffsetY = -distanceY * mCurrentViewport.height() / mContentRect.height();
          computeScrollSurfaceSize(mSurfaceSizeBuffer);
          int scrolledX =
              (int) (mSurfaceSizeBuffer.x * (mCurrentViewport.left + viewportOffsetX - AXIS_X_MIN)
                  / (mXMax - AXIS_X_MIN));
          int scrolledY =
              (int) (mSurfaceSizeBuffer.y * (mYMax - mCurrentViewport.bottom - viewportOffsetY)
                  / (mYMax - mYMin));
          boolean canScrollX =
              mCurrentViewport.left > AXIS_X_MIN || mCurrentViewport.right < mXMax;
          boolean canScrollY =
              mCurrentViewport.top > mYMin || mCurrentViewport.bottom < mYMax;
          setViewportBottomLeft(mCurrentViewport.left + viewportOffsetX,
              mCurrentViewport.bottom + viewportOffsetY);
          if (canScrollX && scrolledX < 0) {
            mEdgeEffectLeft.onPull(scrolledX / (float) mContentRect.width());
            mEdgeEffectLeftActive = true;
          }
          if (canScrollY && scrolledY < 0) {
            mEdgeEffectTop.onPull(scrolledY / (float) mContentRect.height());
            mEdgeEffectTopActive = true;
          }
          if (canScrollX && scrolledX > mSurfaceSizeBuffer.x - mContentRect.width()) {
            mEdgeEffectRight.onPull((scrolledX - mSurfaceSizeBuffer.x + mContentRect.width())
                / (float) mContentRect.width());
            mEdgeEffectRightActive = true;
          }
          if (canScrollY && scrolledY > mSurfaceSizeBuffer.y - mContentRect.height()) {
            mEdgeEffectBottom.onPull((scrolledY - mSurfaceSizeBuffer.y + mContentRect.height())
                / (float) mContentRect.height());
            mEdgeEffectBottomActive = true;
          }
          awakenScrollBars();
          dispatchScrollChanged();
          return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
          if(!isVerticalScrollBarEnabled()){
            velocityY = 0;
          }
          fling((int) -velocityX, (int) -velocityY);
          return true;
        }

        @Override public boolean onDown(MotionEvent e) {
          releaseEdgeEffects();
          mScrollerStartViewport.set(mCurrentViewport);
          mScroller.forceFinished(true);
          ViewCompat.postInvalidateOnAnimation(WaveformView.this);
          return true;
        }

        @Override public boolean onDoubleTap(MotionEvent e) {
          mZoomer.forceFinished(true);
          float x = (mContentRect.left + mContentRect.right) / 2;
          float y = (mContentRect.top + mContentRect.bottom) / 2;
          if (hitTest(x, y, mZoomFocalPoint)) {
            mZoomer.startZoom(ZOOM_AMOUNT);
          }
          awakenScrollBars();
          ViewCompat.postInvalidateOnAnimation(WaveformView.this);
          return true;
        }
      };
  private float mZoomFactor = 1f;
  private final PointF mOriginViewportSize = new PointF();
  /**
   * The scale listener, used for handling multi-finger scale gestures.
   */
  private final ScaleGestureDetector.OnScaleGestureListener mScaleGestureListener =
      new ScaleGestureDetector.SimpleOnScaleGestureListener() {
        /**
         * This is the active focal point in terms of the viewport. Could be a local
         * variable but kept here to minimize per-frame allocations.
         */
        private final PointF viewportFocus = new PointF();
        private float lastSpanX;
        private float lastSpanY;

        @Override public boolean onScale(ScaleGestureDetector scaleGestureDetector) {

          float spanX = ScaleGestureDetectorCompat.getCurrentSpanX(scaleGestureDetector);
          float spanY = ScaleGestureDetectorCompat.getCurrentSpanY(scaleGestureDetector);
          mZoomFactor = mZoomFactor * lastSpanX / spanX;
          mZoomFactor = Math.max(ZOOM_MIN, Math.min(ZOOM_MAX, mZoomFactor));
          float newWidth = mZoomFactor * mOriginViewportSize.x;
          float newHeight = mZoomFactor * mOriginViewportSize.y;

          float focusX = scaleGestureDetector.getFocusX();
          float focusY = scaleGestureDetector.getFocusY();

          if(!isVerticalScrollBarEnabled()) {
            focusY = (mContentRect.top + mContentRect.bottom) / 2;
          }

          hitTest(focusX, focusY, viewportFocus);
          mCurrentViewport.set(
              viewportFocus.x - newWidth * ((focusX - mContentRect.left) / mContentRect.width()),
              viewportFocus.y - newHeight * ((mContentRect.bottom - focusY)
                  / mContentRect.height()), 0, 0);

          mCurrentViewport.right = mCurrentViewport.left + newWidth;
          mCurrentViewport.bottom = mCurrentViewport.top + newHeight;
          constrainViewport();
          ViewCompat.postInvalidateOnAnimation(WaveformView.this);

          lastSpanX = spanX;
          lastSpanY = spanY;
          awakenScrollBars();
          dispatchScrollChanged();
          return true;
        }

        @Override public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
          lastSpanX = ScaleGestureDetectorCompat.getCurrentSpanX(scaleGestureDetector);
          lastSpanY = ScaleGestureDetectorCompat.getCurrentSpanY(scaleGestureDetector);
          return true;
        }
      };
  private Paint mLinePaint;

  public WaveformView(Context context) {
    this(context, null, 0);
  }

  public WaveformView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public WaveformView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    TypedArray a = context.getTheme()
        .obtainStyledAttributes(attrs, R.styleable.WaveformView, defStyle, defStyle);

    try {
      mGridColor = a.getColor(R.styleable.WaveformView_gridColor, mGridColor);

      mGrid1Thickness =
          a.getDimension(R.styleable.WaveformView_grid1Thickness, mGrid1Thickness);

      mGrid2Thickness =
          a.getDimension(R.styleable.WaveformView_grid2Thickness, mGrid2Thickness);
      mGrid2Thickness = 1;
      mDataThickness =
          a.getDimension(R.styleable.WaveformView_dataThickness, mDataThickness);

      m1MMPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 1,
          getResources().getDisplayMetrics());

    } finally {
      a.recycle();
    }

    initPaints();

    // Sets up interactions
    mScaleGestureDetector = new ScaleGestureDetector(context, mScaleGestureListener);
    mGestureDetector = new GestureDetectorCompat(context, mGestureListener);

    mScroller = new OverScroller(context);
    mZoomer = new Zoomer(context);

    // Sets up edge effects
    mEdgeEffectLeft = new EdgeEffect(context);
    mEdgeEffectTop = new EdgeEffect(context);
    mEdgeEffectRight = new EdgeEffect(context);
    mEdgeEffectBottom = new EdgeEffect(context);

  }

  /**
   * (Re)initializes {@link Paint} objects based on current attribute values.
   */
  private void initPaints() {

    mGridPaint1 = new Paint();
    mGridPaint1.setStrokeWidth(mGrid1Thickness);
    mGridPaint1.setColor(mGridColor);
    mGridPaint1.setStyle(Paint.Style.STROKE);

    mGridPaint2 = new Paint();
    mGridPaint2.setStrokeWidth(mGrid2Thickness);
    mGridPaint2.setColor(mGridColor);
    mGridPaint2.setStyle(Paint.Style.STROKE);

    mLinePaint = new Paint();
    mLinePaint.setStrokeWidth(mDataThickness);
    mLinePaint.setStyle(Paint.Style.STROKE);
    mLinePaint.setAntiAlias(true);

  }

  /**
   * Computes the pixel offset for the given X chart value. This may be outside the view bounds.
   */
  private float getDrawX(float x) {
    return mContentRect.left + mContentRect.width() * (x - mCurrentViewport.left) / mCurrentViewport
        .width();
  }

  /**
   * Computes the pixel offset for the given Y chart value. This may be outside the view bounds.
   */
  private float getDrawY(float y) {
    return mContentRect.bottom
        - mContentRect.height() * (y - mCurrentViewport.top) / mCurrentViewport.height();
  }

  private void drawGrid(Canvas canvas) {
    int index = 0;
    for (float f = (float) (Math.floor(mCurrentViewport.top) - 1); f < mCurrentViewport.bottom + 1;
        f += 1) {
      mDrawBuffer[index] = mContentRect.left;
      mDrawBuffer[index + 1] = getDrawY(f);
      mDrawBuffer[index + 2] = mContentRect.right;
      mDrawBuffer[index + 3] = mDrawBuffer[index + 1];
      if (f % 5 == 0) {
        canvas.drawLines(mDrawBuffer, index, 4, mGridPaint1);
      } else {
        index += 4;
      }
      if(index == mDrawBuffer.length){
        index =0;
        canvas.drawLines(mDrawBuffer, 0, index, mGridPaint2);
      }
    }
    canvas.drawLines(mDrawBuffer, 0, index, mGridPaint2);
    index =0 ;

    for (float f = (float) (Math.floor(mCurrentViewport.left) - 1); f < mCurrentViewport.right + 1;
        f += 1) {
      mDrawBuffer[index] = getDrawX(f);
      mDrawBuffer[index + 1] = mContentRect.top;
      mDrawBuffer[index + 2] = mDrawBuffer[index];
      mDrawBuffer[index + 3] = mContentRect.bottom;
      if (f % 5 == 0) {
        canvas.drawLines(mDrawBuffer, index, 4, mGridPaint1);
      } else {
        index += 4;
      }
      if(index == mDrawBuffer.length){
        index =0;
        canvas.drawLines(mDrawBuffer, 0, index, mGridPaint2);
      }
    }
    canvas.drawLines(mDrawBuffer, 0, index, mGridPaint2);
  }
  
  /**
   * Draws the overscroll "glow" at the four edges of the chart region, if necessary. The edges
   * of the chart region are stored in {@link #mContentRect}.
   *
   * @see EdgeEffect
   */
  private void drawEdgeEffectsUnclipped(Canvas canvas) {
    // The methods below rotate and translate the canvas as needed before drawing the glow,
    // since EdgeEffectalways draws a top-glow at 0,0.

    boolean needsInvalidate = false;

    if (!mEdgeEffectTop.isFinished()) {
      final int restoreCount = canvas.save();
      canvas.translate(mContentRect.left, mContentRect.top);
      mEdgeEffectTop.setSize(mContentRect.width(), mContentRect.height());
      if (mEdgeEffectTop.draw(canvas)) {
        needsInvalidate = true;
      }
      canvas.restoreToCount(restoreCount);
    }

    if (!mEdgeEffectBottom.isFinished()) {
      final int restoreCount = canvas.save();
      canvas.translate(2 * mContentRect.left - mContentRect.right, mContentRect.bottom);
      canvas.rotate(180, mContentRect.width(), 0);
      mEdgeEffectBottom.setSize(mContentRect.width(), mContentRect.height());
      if (mEdgeEffectBottom.draw(canvas)) {
        needsInvalidate = true;
      }
      canvas.restoreToCount(restoreCount);
    }

    if (!mEdgeEffectLeft.isFinished()) {
      final int restoreCount = canvas.save();
      canvas.translate(mContentRect.left, mContentRect.bottom);
      canvas.rotate(-90, 0, 0);
      mEdgeEffectLeft.setSize(mContentRect.height(), mContentRect.width());
      if (mEdgeEffectLeft.draw(canvas)) {
        needsInvalidate = true;
      }
      canvas.restoreToCount(restoreCount);
    }

    if (!mEdgeEffectRight.isFinished()) {
      final int restoreCount = canvas.save();
      canvas.translate(mContentRect.right, mContentRect.top);
      canvas.rotate(90, 0, 0);
      mEdgeEffectRight.setSize(mContentRect.height(), mContentRect.width());
      if (mEdgeEffectRight.draw(canvas)) {
        needsInvalidate = true;
      }
      canvas.restoreToCount(restoreCount);
    }

    if (needsInvalidate) {
      ViewCompat.postInvalidateOnAnimation(this);
    }
  }

  /**
   * Finds the chart point (i.e. within the chart's domain and range) represented by the
   * given pixel coordinates, if that pixel is within the chart region described by
   * {@link #mContentRect}. If the point is found, the "dest" argument is set to the point and
   * this function returns true. Otherwise, this function returns false and "dest" is unchanged.
   */
  private boolean hitTest(float x, float y, PointF dest) {
    if (!mContentRect.contains((int) x, (int) y)) {
      return false;
    }

    dest.set(mCurrentViewport.left + mCurrentViewport.width() * ((x - mContentRect.left)
            / mContentRect.width()),
        mCurrentViewport.top + mCurrentViewport.height() * ((y - mContentRect.bottom)
            / -mContentRect.height()));
    return true;
  }

  @Override public boolean onTouchEvent(MotionEvent event) {
    boolean retVal = mScaleGestureDetector.onTouchEvent(event);
    retVal = mGestureDetector.onTouchEvent(event) || retVal;
    return retVal || super.onTouchEvent(event);
  }

  @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    mContentRect.set(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(),
        getHeight() - getPaddingBottom());

    mOriginViewportSize.set(mContentRect.width() / m1MMPx, mContentRect.height() / m1MMPx);
    mYMax = mOriginViewportSize.y / 2;
    mYMin = -mYMax;

    mXMax = Math.max(mOriginViewportSize.x, getDataSize() / POINTS_PRE_MM);

    mCurrentViewport.set(mCurrentViewport.left, -mOriginViewportSize.y / 2 * mZoomFactor,
        mCurrentViewport.left + mOriginViewportSize.x * mZoomFactor,
        mOriginViewportSize.y / 2 * mZoomFactor);
    if(mCurrentViewport.right > mXMax){
      mCurrentViewport.left -=  mCurrentViewport.right - mXMax;
      mCurrentViewport.right = mXMax;
    }
    computeScrollSurfaceSize(mSurfaceSizeBuffer);
    dispatchScrollChanged();
  }

  private int getDataSize(){
    int size =0;
    for(DataInfo item:mData){
      size = Math.max(item.data.size(), size);
    }
    return size;
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////
  //
  //     Methods and objects related to gesture handling
  //
  ////////////////////////////////////////////////////////////////////////////////////////////////

  @Override public void computeScroll() {
    super.computeScroll();
    boolean needsInvalidate = false;

    if (mScroller.computeScrollOffset()) {
      // The scroller isn't finished, meaning a fling or programmatic pan operation is
      // currently active.

      computeScrollSurfaceSize(mSurfaceSizeBuffer);
      int currX = mScroller.getCurrX();
      int currY = mScroller.getCurrY();

      boolean canScrollX =
          (mCurrentViewport.left > AXIS_X_MIN || mCurrentViewport.right < mXMax);
      boolean canScrollY =
          (mCurrentViewport.top > mYMin || mCurrentViewport.bottom < mYMax);
      if (canScrollX && currX < 0 && mEdgeEffectLeft.isFinished() && !mEdgeEffectLeftActive) {
        mEdgeEffectLeft.onAbsorb((int) OverScrollerCompat.getCurrVelocity(mScroller));
        mEdgeEffectLeftActive = true;
        needsInvalidate = true;
      } else if (canScrollX
          && currX > (mSurfaceSizeBuffer.x - mContentRect.width())
          && mEdgeEffectRight.isFinished()
          && !mEdgeEffectRightActive) {
        mEdgeEffectRight.onAbsorb((int) OverScrollerCompat.getCurrVelocity(mScroller));
        mEdgeEffectRightActive = true;
        needsInvalidate = true;
      }

      if (canScrollY && currY < 0 && mEdgeEffectTop.isFinished() && !mEdgeEffectTopActive) {
        mEdgeEffectTop.onAbsorb((int) OverScrollerCompat.getCurrVelocity(mScroller));
        mEdgeEffectTopActive = true;
        needsInvalidate = true;
      } else if (canScrollY
          && currY > (mSurfaceSizeBuffer.y - mContentRect.height())
          && mEdgeEffectBottom.isFinished()
          && !mEdgeEffectBottomActive) {
        mEdgeEffectBottom.onAbsorb((int) OverScrollerCompat.getCurrVelocity(mScroller));
        mEdgeEffectBottomActive = true;
        needsInvalidate = true;
      }

      float currXRange = AXIS_X_MIN + (mXMax - AXIS_X_MIN) * currX / mSurfaceSizeBuffer.x;
      float currYRange = mYMax - (mYMax - mYMin) * currY / mSurfaceSizeBuffer.y;
      if(!isVerticalScrollBarEnabled()){
        currYRange = mCurrentViewport.bottom;
      }
      setViewportBottomLeft(currXRange, currYRange);
    }

    if (mZoomer.computeZoom()) {
      // Performs the zoom since a zoom is in progress (either programmatically or via
      // double-touch).

      float newWidth = (1f - mZoomer.getCurrZoom()) * mScrollerStartViewport.width();
      float newHeight = (1f - mZoomer.getCurrZoom()) * mScrollerStartViewport.height();
      newWidth = Math.max(newWidth, mOriginViewportSize.x * ZOOM_MIN);
      newHeight = Math.max(newHeight, mOriginViewportSize.y * ZOOM_MIN);
      mZoomFactor = newWidth / mOriginViewportSize.x;

      float pointWithinViewportX =
          (mZoomFocalPoint.x - mScrollerStartViewport.left) / mScrollerStartViewport.width();
      float pointWithinViewportY =
          (mZoomFocalPoint.y - mScrollerStartViewport.top) / mScrollerStartViewport.height();
      mCurrentViewport.set(mZoomFocalPoint.x - newWidth * pointWithinViewportX,
          mZoomFocalPoint.y - newHeight * pointWithinViewportY,
          mZoomFocalPoint.x + newWidth * (1 - pointWithinViewportX),
          mZoomFocalPoint.y + newHeight * (1 - pointWithinViewportY));
      constrainViewport();
      computeScrollSurfaceSize(mSurfaceSizeBuffer);
      needsInvalidate = true;
    }
    dispatchScrollChanged();
    if (needsInvalidate) {
      ViewCompat.postInvalidateOnAnimation(this);
    }
  }

  @Override protected int computeHorizontalScrollRange() {
    return mSurfaceSizeBuffer.x;
  }

  @Override protected int computeHorizontalScrollOffset() {
    return (int) (mSurfaceSizeBuffer.x * (mCurrentViewport.left / mXMax));
  }

  @Override protected int computeHorizontalScrollExtent() {
    return mContentRect.width();
  }

  @Override protected int computeVerticalScrollRange() {
    return mSurfaceSizeBuffer.y;
  }

  @Override protected int computeVerticalScrollOffset() {
    return (int) (mSurfaceSizeBuffer.y * ((mYMax - mCurrentViewport.bottom ) / (mYMax - mYMin)));
  }

  @Override protected int computeVerticalScrollExtent() {
    return mContentRect.height();
  }

  @Override protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    int clipRestoreCount = canvas.save();
    canvas.clipRect(mContentRect);

    drawGrid(canvas);
    float mid = (mData.size() -1)/ 2f ;
    for(int i =0;i <mData.size() ;++i){
      drawData(canvas, mData.get(i).data, - (i - mid) * 2, mData.get(i).color);
    }
    drawEdgeEffectsUnclipped(canvas);

    canvas.restoreToCount(clipRestoreCount);
  }

  private void drawData(Canvas canvas, ArrayList<Float> dataList, float yOffSet, int color) {
    int start = (int) (Math.floor(mCurrentViewport.left * POINTS_PRE_MM) - 1);
    int end = (int) (Math.ceil(mCurrentViewport.right * POINTS_PRE_MM) + 1);
    start = Math.max(0, start);
    end = Math.min(dataList.size(), end);
    int index = 0;
    int drawStart = start;

    while (drawStart < end){
      index = 0;
      mDrawBuffer[index] = getDrawX(drawStart /POINTS_PRE_MM);
      mDrawBuffer[index + 1] = getDrawY((dataList.get(drawStart) +yOffSet) * 10);
      mDrawBuffer[index + 2] =  mDrawBuffer[index];
      mDrawBuffer[index + 3] =  mDrawBuffer[index + 1];
      index += 4;
      ++drawStart;

      for (; drawStart < end;) {
        mDrawBuffer[index] = mDrawBuffer[index - 2];
        mDrawBuffer[index + 1] = mDrawBuffer[index - 1];
        mDrawBuffer[index + 2] = getDrawX(drawStart / POINTS_PRE_MM);
        mDrawBuffer[index + 3] = getDrawY((dataList.get(drawStart) + yOffSet) * 10);
        index += 4;
        if(index < mDrawBuffer.length){
          ++drawStart;
        }else {
          break;
        }
      }
      mLinePaint.setColor(color);
      canvas.drawLines(mDrawBuffer, 0, index, mLinePaint);
    }

  }

  @Override public Parcelable onSaveInstanceState() {
    Parcelable superState = super.onSaveInstanceState();
    SavedState ss = new SavedState(superState);
    ss.left = mCurrentViewport.left;
    ss.zoomFactor = mZoomFactor;
    return ss;
  }

  @Override public void onRestoreInstanceState(Parcelable state) {
    if (!(state instanceof SavedState)) {
      super.onRestoreInstanceState(state);
      return;
    }

    SavedState ss = (SavedState) state;
    super.onRestoreInstanceState(ss.getSuperState());

    mCurrentViewport.left = ss.left;

    mZoomFactor = ss.zoomFactor;
  }

  /**
   * Ensures that current viewport is inside the viewport extremes defined by {@link #AXIS_X_MIN},
   * {@link #mXMax}, {@link #mYMin} and {@link #mYMax}.
   */
  private void constrainViewport() {
    mCurrentViewport.left = Math.max(AXIS_X_MIN, mCurrentViewport.left);
    mCurrentViewport.top = Math.max(mYMin, mCurrentViewport.top);
    mCurrentViewport.bottom =
        Math.max(Math.nextUp(mCurrentViewport.top), Math.min(mYMax, mCurrentViewport.bottom));
    mCurrentViewport.right =
        Math.max(Math.nextUp(mCurrentViewport.left), Math.min(mXMax, mCurrentViewport.right));
  }

  private void releaseEdgeEffects() {
    mEdgeEffectLeftActive =
        mEdgeEffectTopActive = mEdgeEffectRightActive = mEdgeEffectBottomActive = false;
    mEdgeEffectLeft.onRelease();
    mEdgeEffectTop.onRelease();
    mEdgeEffectRight.onRelease();
    mEdgeEffectBottom.onRelease();
  }

  private void fling(int velocityX, int velocityY) {

    releaseEdgeEffects();
    // Flings use math in pixels (as opposed to math based on the viewport).
    computeScrollSurfaceSize(mSurfaceSizeBuffer);
    mScrollerStartViewport.set(mCurrentViewport);
    int startX = (int) (mSurfaceSizeBuffer.x * ((mScrollerStartViewport.left - AXIS_X_MIN) / (
        mXMax
            - AXIS_X_MIN)));
    int startY = (int) (mSurfaceSizeBuffer.y * ((mYMax - mScrollerStartViewport.bottom) / (
        mYMax
            - mYMin)));
    mScroller.forceFinished(true);
    mScroller.fling(startX, startY, velocityX, velocityY, 0,
        mSurfaceSizeBuffer.x - mContentRect.width(), 0,
        mSurfaceSizeBuffer.y - mContentRect.height(), mContentRect.width() / 2,
        mContentRect.height() / 2);
    awakenScrollBars();
    ViewCompat.postInvalidateOnAnimation(this);
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////
  //
  //     Methods for programmatically changing the viewport
  //
  ////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Computes the current scrollable surface size, in pixels. For example, if the entire chart
   * area is visible, this is simply the current size of {@link #mContentRect}. If the chart
   * is zoomed in 200% in both directions, the returned size will be twice as large horizontally
   * and vertically.
   */
  private void computeScrollSurfaceSize(Point out) {
    out.set((int) (mContentRect.width() * ((mXMax - AXIS_X_MIN) / mCurrentViewport.width())),
        (int) (mContentRect.height() * ((mYMax - mYMin) / mCurrentViewport.height())));
  }

  /**
   * Sets the current viewport (defined by {@link #mCurrentViewport}) to the given
   * X and Y positions. Note that the Y value represents the topmost pixel position, and thus
   * the bottom of the {@link #mCurrentViewport} rectangle. For more details on why top and
   * bottom are flipped, see {@link #mCurrentViewport}.
   */
  private void setViewportBottomLeft(float x, float y) {
    /**
     * Constrains within the scroll range. The scroll range is simply the viewport extremes
     * (mXMax, etc.) minus the viewport size. For example, if the extrema were 0 and 10,
     * and the viewport size was 2, the scroll range would be 0 to 8.
     */

    float curWidth = mCurrentViewport.width();
    float curHeight = mCurrentViewport.height();
    x = Math.max(AXIS_X_MIN, Math.min(x, mXMax - curWidth));
    y = Math.max(mYMin + curHeight, Math.min(y, mYMax));

    mCurrentViewport.set(x, y - curHeight, x + curWidth, y);
    ViewCompat.postInvalidateOnAnimation(this);
  }

  /**
   * Returns the current viewport (visible extremes for the chart domain and range.)
   */
  public RectF getCurrentViewport() {
    return new RectF(mCurrentViewport);
  }

  /**
   * Sets the chart's current viewport.
   *
   * @see #getCurrentViewport()
   */
  public void setCurrentViewport(RectF viewport) {
    mCurrentViewport = viewport;
    constrainViewport();
    ViewCompat.postInvalidateOnAnimation(this);
  }

  protected void dispatchScrollChanged(){
    if(mOnScrollListener != null){
      float start = mCurrentViewport.left * 0.04f;
      float end =  mCurrentViewport.right *0.04f;
      mOnScrollListener.onDurationChanged(start, end);
    }
  }

  public void setOnScrollListener(OnScrollListener onScrollListener) {
    this.mOnScrollListener = onScrollListener;
  }
  /**
   * Smoothly zooms the chart in one step.
   */
  public void zoomIn() {
    awakenScrollBars();
    mScrollerStartViewport.set(mCurrentViewport);
    mZoomer.forceFinished(true);
    mZoomer.startZoom(ZOOM_AMOUNT);
    mZoomFocalPoint.set((mCurrentViewport.right + mCurrentViewport.left) / 2,
        (mCurrentViewport.bottom + mCurrentViewport.top) / 2);
    ViewCompat.postInvalidateOnAnimation(this);
  }

  /**
   * Smoothly zooms the chart out one step.
   */
  public void zoomOut() {
    mScrollerStartViewport.set(mCurrentViewport);
    mZoomer.forceFinished(true);
    mZoomer.startZoom(-ZOOM_AMOUNT);
    mZoomFocalPoint.set((mCurrentViewport.right + mCurrentViewport.left) / 2,
        (mCurrentViewport.bottom + mCurrentViewport.top) / 2);
    ViewCompat.postInvalidateOnAnimation(this);
  }

  /**
   * Smoothly pans the chart left one step.
   */
  public void panLeft() {
    fling((int) (-PAN_VELOCITY_FACTOR * getWidth()), 0);
  }

  /**
   * Smoothly pans the chart right one step.
   */
  public void panRight() {
    fling((int) (PAN_VELOCITY_FACTOR * getWidth()), 0);
  }

  /**
   * Smoothly pans the chart up one step.
   */
  public void panUp() {
    //  fling(0, (int) (-PAN_VELOCITY_FACTOR * getHeight()));
  }

  /**
   * Smoothly pans the chart down one step.
   */
  public void panDown() {
    // fling(0, (int) (PAN_VELOCITY_FACTOR * getHeight()));
  }

  public float getDataThickness() {
    return mDataThickness;
  }

  public void setDataThickness(float dataThickness) {
    mDataThickness = dataThickness;
  }


  public void addData(ArrayList<Float> data,    @ColorInt int lineColor){
    mData.add(new DataInfo(data, lineColor));
    onDataChanged();
  }


  private void onDataChanged() {
    mXMax = Math.max(mOriginViewportSize.x, getDataSize()  /POINTS_PRE_MM);
    computeScrollSurfaceSize(mSurfaceSizeBuffer);
    awakenScrollBars();
    dispatchScrollChanged();
    invalidate();

  }

  ////////////////////////////////////////////////////////////////////////////////////////////////
  //
  //     Methods and classes related to view state persistence.
  //
  ////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Persistent state that is saved by InteractiveLineGraphView.
   */
  public static class SavedState extends BaseSavedState {
    public static final Parcelable.Creator<SavedState> CREATOR =
        new ClassLoaderCreator<SavedState>() {
          @Override public SavedState createFromParcel(Parcel source, ClassLoader loader) {
            return new SavedState(source);
          }

          @Override public SavedState createFromParcel(Parcel source) {
            return new SavedState(source);
          }

          @Override public SavedState[] newArray(int size) {
            return new SavedState[size];
          }
        };
    private float left;
    private float zoomFactor;

    public SavedState(Parcelable superState) {
      super(superState);
    }

    SavedState(Parcel in) {
      super(in);
      left = in.readFloat();
      zoomFactor = in.readFloat();
    }

    @Override public void writeToParcel(Parcel out, int flags) {
      super.writeToParcel(out, flags);
      out.writeFloat(left);
      out.writeFloat(zoomFactor);
    }

    @Override public String toString() {
      return "InteractiveLineGraphView.SavedState{" + Integer.toHexString(
          System.identityHashCode(this)) + " left=" + left + ", zoomFactor =" + zoomFactor + "}";
    }
  }

  private static class DataInfo{
    ArrayList<Float> data;
    int color;

    public DataInfo(ArrayList<Float> data, int color) {
      this.data = data;
      this.color = color;
    }
  }
}
