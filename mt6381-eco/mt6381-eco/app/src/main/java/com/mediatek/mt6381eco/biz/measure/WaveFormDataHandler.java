package com.mediatek.mt6381eco.biz.measure;

import java.util.ArrayDeque;
import java.util.List;

abstract class WaveFormDataHandler {
  static final int DATA_LIMIT = 512;
  static final int SKIP_COUNT = 512;
  private final List<Float> data;
  int index = 0;
  private float sumMv = 0f;
  private long mLastTimestamp = 0L;
  private int mDataCount = 0;
  private final ArrayDeque<Integer> mBufferData = new ArrayDeque<>();

  protected WaveFormDataHandler(List<Float> data) {
    this.data = data;
  }

  public synchronized void receiveData(int value) {
    mBufferData.add(value);
  }

  public synchronized void invalidate() {
    long now = System.currentTimeMillis();
    if (mLastTimestamp < 1) {
      mLastTimestamp = now;
      mDataCount = 0;
    }
    int releaseCount = (int) ((now - mLastTimestamp) / (1000f / 512)) - mDataCount;
    for (int i = 0; i < releaseCount; ++i) {
      Integer value = mBufferData.poll();
      if (value == null) {
        break;
      }
      ++mDataCount;
      toDraw(value);
    }

    if (mBufferData.isEmpty()) {
      mDataCount = 0;
      mLastTimestamp = 0L;
    }
    //Timber.d("releaseCount:%d -%d", releaseCount, mBufferData.size());
  }

  private void toDraw(int value) {
    synchronized (data) {
      if (index >= SKIP_COUNT) {
        if (data.size() >= DATA_LIMIT) {
          data.clear();
        }
        sumMv += toMv(value);
        if (index % 4 == 3) {
          float avgMv = sumMv / 4f;
          sumMv = 0f;
          float filtedmv = filter(avgMv);
          data.add(filtedmv);
        }
      }
      index++;
    }
  }

  abstract float toMv(int value);

  abstract float filter(float value);
}