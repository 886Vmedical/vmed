package com.mediatek.mt6381eco.ui;

import android.os.Handler;
import android.os.Looper;

public class TaskExecutor {

  private static TaskExecutor sInstance;
  private final Object mLock = new Object();
  private Handler mMainHandler;

  public static TaskExecutor getInstance() {
    if (sInstance != null) {
      return sInstance;
    }
    synchronized (TaskExecutor.class) {
      if (sInstance == null) {
        sInstance = new TaskExecutor();
      }
    }
    return sInstance;
  }

  public boolean isMainThread() {
    return Looper.getMainLooper().getThread() == Thread.currentThread();
  }

  public void postToMainThread(Runnable runnable) {
    if (mMainHandler == null) {
      synchronized (mLock) {
        if (mMainHandler == null) {
          mMainHandler = new Handler(Looper.getMainLooper());
        }
      }
    }
    mMainHandler.post(runnable);
  }
}
