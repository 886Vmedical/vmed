package com.mediatek.mt6381eco.biz.measure;

public class IntArrayVersion {
  private Object[] mLastData = null;

  public boolean isChanged(Object... args) {
    if (mLastData == null || mLastData.length != args.length) {
      mLastData = args;
      return true;
    }
    int i = 0;
    while (i < mLastData.length && i < args.length && equal(mLastData[i], args[i])) {
      ++i;
    }
    if (i == mLastData.length) {
      return false;
    }
    mLastData = args;
    return true;
  }

  private boolean equal(Object obj1, Object obj2){
    if(obj1 instanceof Comparable && obj2 instanceof Comparable){
      return ((Comparable) obj1).compareTo(obj2) == 0;
    }
    return  obj1 == obj2;

  }

}
