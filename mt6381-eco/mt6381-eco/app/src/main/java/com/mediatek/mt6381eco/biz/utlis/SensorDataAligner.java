package com.mediatek.mt6381eco.biz.utlis;

import android.util.SparseArray;
import com.mediatek.mt6381eco.biz.measure.DataLostEvent;
import com.mediatek.mt6381eco.rxbus.RxBus;
import java.util.ArrayDeque;
import java.util.Deque;
import timber.log.Timber;

public class SensorDataAligner {
  private final SparseArray<DataPackage> mDataMap = new SparseArray<>();
  private int[] mBuffer;
  private int maxFirstSn = -1;

  public int[] align(int type, int sn, int value) {
    DataPackage dataPackage = mDataMap.get(type);
    if (dataPackage != null) {
      add(dataPackage, type, sn, value);
      alignHeadIfNeed();
      return popAlignData();
    }
    return null;
  }

  private void alignHeadIfNeed() {
    for (int i = 0; i < mDataMap.size(); ++i) {
      DataPackage dataPackage = mDataMap.valueAt(i);
      while (!dataPackage.data.isEmpty() && dataPackage.minSn < maxFirstSn) {
        dataPackage.data.pop();
        ++dataPackage.minSn;
      }
    }
  }

  private int[] popAlignData() {
    boolean isReady = true;
    for (int i = 0; i < mDataMap.size(); ++i) {
      DataPackage dataPackage = mDataMap.valueAt(i);
      if (dataPackage.data.isEmpty() || dataPackage.minSn != maxFirstSn) {
        isReady = false;
        break;
      }
    }
    if (isReady) {
      for (int i = 0; i < mDataMap.size(); ++i) {
        DataPackage dataPackage = mDataMap.valueAt(i);
        mBuffer[i] = dataPackage.data.pop();
        ++dataPackage.minSn;
      }
      return mBuffer;
    }
    return null;
  }

  private void add(DataPackage dataPackage, int type, int sn, int value) {

    if (sn != dataPackage.getMaxSn() + 1) {
      Timber.d("data lost");
      RxBus.getInstance().post(new DataLostEvent(type));

      dataPackage.data.clear();
    }
    if (dataPackage.data.size() == 0) {
      dataPackage.minSn = sn;
      maxFirstSn = Math.max(sn, maxFirstSn);
    }
    dataPackage.data.add(value);
  }

  public void init(int[] dataTypes) {
    mDataMap.clear();
    maxFirstSn = -1;
    for (int dataType : dataTypes) {
      mDataMap.put(dataType, new DataPackage());
    }
    mBuffer = new int[dataTypes.length];
  }

  private static class DataPackage {
    private final Deque<Integer> data = new ArrayDeque<>();
    private int minSn = 0;

    int getMaxSn() {
      return minSn + data.size() - 1;
    }
  }
}
