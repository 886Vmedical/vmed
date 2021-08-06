package com.mediatek.mt6381.ble;

import android.util.SparseArray;
import com.mediatek.mt6381eco.biz.peripheral.SensorData;
import java.util.ArrayDeque;

public class DataAligner {
  private SensorData[] mBuffer;

  private final SparseArray<ArrayDeque<SensorData>> mDataMap = new SparseArray<>(3);
  private int mHeadMaxSn = 0;

  public  SensorData[] align(SensorData sensorData) {
    //Timber.d("parsed_SensorData - type:%d - sn:%d", sensorData.type, sensorData.sn);
    int dataType = sensorData.type;
    ArrayDeque<SensorData> queue = mDataMap.get(dataType);
    if (queue != null) {
      queue.add(sensorData);
      mHeadMaxSn = Math.max(mHeadMaxSn, queue.peek().sn);
      return judgeAndPostData();
    }
    return null;
  }

  private  SensorData[] judgeAndPostData() {
    dropDataLost();
    boolean canPost = false;
    for (int i = 0; i < mDataMap.size(); ++i) {
      ArrayDeque<SensorData> queue = mDataMap.valueAt(i);
      if (queue.peek() == null || queue.peek().sn != mHeadMaxSn) {
        canPost = false;
        break;
      } else {
        canPost = true;
      }
    }
    if (canPost) {
      for (int i = 0; i < mDataMap.size(); ++i) {
        mBuffer[i] = mDataMap.valueAt(i).poll();
      }
      return mBuffer;
    }
    return null;
  }

  private void dropDataLost() {
    for (int i = 0; i < mDataMap.size(); ++i) {
      ArrayDeque<SensorData> queue = mDataMap.valueAt(i);
      while (queue.peek() != null && queue.peek().sn < mHeadMaxSn) {
        queue.poll();
      }
    }
  }

  public void reset(int[] dataTypes) {
    mDataMap.clear();
    for (int type : dataTypes) {
      mDataMap.put(type, new ArrayDeque<>());
    }
    mBuffer = new SensorData[dataTypes.length];
    mHeadMaxSn = 0;
  }
}
