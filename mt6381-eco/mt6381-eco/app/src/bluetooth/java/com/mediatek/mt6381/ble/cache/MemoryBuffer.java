package com.mediatek.mt6381.ble.cache;

import java.util.ArrayList;
import timber.log.Timber;

public class MemoryBuffer<T> {
  private final int mBufferSize;
  private final ArrayList<T> mBuffer;

  public MemoryBuffer(int bufferSize) {
    mBufferSize = bufferSize;
    mBuffer = new ArrayList<>(bufferSize);
  }

  public void release(T obj) {
    if (mBuffer.size() < mBufferSize) {
      mBuffer.add(obj);
    } else {
      Timber.w("buffer is full");
    }
  }

  public T fetch() {
    if (mBuffer.size() > 0) {
      return mBuffer.remove(mBuffer.size() - 1);
    } else {
      return null;
    }
  }

  public void clear() {
    mBuffer.clear();
  }
}
