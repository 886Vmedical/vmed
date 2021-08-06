package com.mediatek.mt6381eco.biz.utlis;

import android.util.SparseIntArray;
import timber.log.Timber;

public class FingerOffChecker {
  private static final int STATUS_FINGER_OFF = 0;
  private static final int THRESHOLD_FINGER_OFF = 32;
  private final SparseIntArray mMapStatus = new SparseIntArray();

  public boolean isFingerOff(int type, int status) {
    int offCount = mMapStatus.get(type, 0);
    if (status == STATUS_FINGER_OFF) {
      ++offCount;
    } else {
      offCount = 0;
    }
    mMapStatus.put(type, offCount);
    return offCount >= THRESHOLD_FINGER_OFF;
  }

  public void reset() {
    Timber.i("THRESHOLD_FINGER_OFF:%d", THRESHOLD_FINGER_OFF);
    mMapStatus.clear();
  }
}
