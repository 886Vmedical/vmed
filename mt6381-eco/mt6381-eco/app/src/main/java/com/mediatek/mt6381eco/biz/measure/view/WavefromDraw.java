package com.mediatek.mt6381eco.biz.measure.view;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.ProcessingInstruction;

public class WavefromDraw {
  private static final int PAGE_COUNT = 512 ;
  private final Paint paint;
  private float xFactor = 1;
  private float yFactor = 1;
  private int width;
  private int height;
  private final Path path = new Path();
  private final Drawable icon;
  private final Rect mRect = new Rect();
  private List<Float> mData = new ArrayList<>();
  private final float[] mLineBuffer = new float[PAGE_COUNT * 4];

  public WavefromDraw(Drawable icon, Paint paint) {
    this.paint = paint;
    this.icon = icon;
    this.icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
  }

  public void setData(List<Float> data) {
    mData = data;
  }

  public int getCount(){
    return mData.size();
  }

  private float toX(int index) {
    return index * xFactor;
  }

  private float toY(float mv) {
    //change yfactor draw orientation by krestin
    return -(mv * yFactor);
  }

  public void draw(Canvas canvas) {
    float fx1 = -1;
    float fy1 = -1;
    int index = 0;
    synchronized (mData) {
      int i = 0;
      for (float value : mData) {
        float fx2 = toX(i);
        float fy2 = toY(value);
        if (i > 0) {
          //canvas.drawLine(fx1, fy1, fx2, fy2, paint);
          mLineBuffer[index ++] = fx1;
          mLineBuffer[index ++] = fy1;
          mLineBuffer[index ++] = fx2;
          mLineBuffer[index ++] = fy2;
        } else {
          //canvas.drawLine(fx2, fy2, fx2, fy2, paint);
          mLineBuffer[index ++] = fx2;
          mLineBuffer[index ++] = fy2;
          mLineBuffer[index ++] = fx2;
          mLineBuffer[index ++] = fy2;

        }
        fx1 = fx2;
        fy1 = fy2;
        ++i;
        if(index >= mLineBuffer.length){
          break;
        }
      }
    }
    canvas.drawLines(mLineBuffer, 0, index, paint);
    mRect.set(icon.getBounds());
    mRect.offsetTo((int) fx1, (int) (fy1 - mRect.height() / 3));
    icon.setBounds(mRect);
    icon.draw(canvas);
  }

  public void layout(int width, int height) {
    this.width = width;
    this.height = height;
    xFactor = (float) width / (float) PAGE_COUNT;
    yFactor = height / 2;
  }
}
