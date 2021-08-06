package com.mediatek.mt6381eco.ui;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;
import java.util.concurrent.ConcurrentLinkedQueue;

public class UIActionExecutor implements LifecycleObserver {
  private final LifecycleOwner mOwner;
  private final ConcurrentLinkedQueue<Action> mActions = new ConcurrentLinkedQueue<>();
  private final Runnable mRunnable = this::onStateChange;

  public UIActionExecutor(LifecycleOwner owner) {
    this.mOwner = owner;
    mOwner.getLifecycle().addObserver(this);
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_ANY) void onStateChange() {
    if (mOwner.getLifecycle().getCurrentState() == Lifecycle.State.DESTROYED) {
      mOwner.getLifecycle().removeObserver(this);
    } else if (isUiReady()) {
      Action action;
      while ((action = mActions.poll()) != null) {
        action.call();
      }
    }
  }

  public void uiAction(Action action) {
    mActions.add(action);
    if (TaskExecutor.getInstance().isMainThread()) {
      onStateChange();
    } else {
      TaskExecutor.getInstance().postToMainThread(mRunnable);
    }
  }

  public void postUIAciton(Action action){
    mActions.add(action);
    TaskExecutor.getInstance().postToMainThread(mRunnable);
  }

  private boolean isUiReady() {
    return mOwner.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED);
  }

  public interface Action {
    void call();
  }
}
