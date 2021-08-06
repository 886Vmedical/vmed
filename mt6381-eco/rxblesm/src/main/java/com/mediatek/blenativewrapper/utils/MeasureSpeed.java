package com.mediatek.blenativewrapper.utils;

import java.util.Arrays;

public class MeasureSpeed {
  private final int mStep;
  private final int[] mCountArray;
  private int mLastIndex = 0;
  private long mLastTime = -1L;
  private long mFirstTime = -1L;

  public MeasureSpeed(int measureUnitTimeInMillis, int bufferSize) {
    mCountArray = new int[bufferSize];
    mStep = measureUnitTimeInMillis / bufferSize;
  }

  public synchronized void reset() {
    mLastIndex = 0;
    mLastTime = -1L;
    mFirstTime = -1L;
    Arrays.fill(mCountArray, 0);
  }

  public synchronized void receive(int count) {
    receive(count, System.currentTimeMillis());
  }

  protected void receive(int count, long now) {
    now = now / mStep * mStep;
    if (mLastTime < 0) mLastTime = now;
    if (mFirstTime < 0) mFirstTime = now;
    int step = (int) ((now - mLastTime) / mStep);
    //if step is overflow int
    if(step < 0){
      step = mCountArray.length;
    }
    int thisIndex = mLastIndex + step;
    Arrays.fill(mCountArray, mLastIndex + 1, Math.min(mCountArray.length, thisIndex + 1), 0);
    if (thisIndex >= mCountArray.length) {
      Arrays.fill(mCountArray, 0, Math.min(mLastIndex, thisIndex - mCountArray.length) + 1, 0);
    }
    thisIndex = thisIndex % mCountArray.length;
    mCountArray[thisIndex] += count;
    mLastIndex = thisIndex;
    mLastTime = now;
  }

  public synchronized long bps() {
    return bps(System.currentTimeMillis());
  }

  protected long bps(long now) {
    receive(0, now);
    int count = (int) Math.min(mCountArray.length, (now - mFirstTime) / mStep + 1);
    if (count < 1) {
      count = 1;
    }
    int beginIndex = mLastIndex - count + 1;
    long sum = 0;
    sum += sum(mCountArray, Math.max(0, beginIndex), mLastIndex + 1);
    if (beginIndex < 0) {
      sum += sum(mCountArray, beginIndex + mCountArray.length, mCountArray.length);
    }
    return (long) (sum * 1000f / (count * mStep));
  }

  private int sum(int[] data, int fromIndex, int toIndex) {
    int sum = 0;
    for (int i = fromIndex; i < toIndex; ++i) {
      sum += data[i];
    }
    return sum;
  }
}
