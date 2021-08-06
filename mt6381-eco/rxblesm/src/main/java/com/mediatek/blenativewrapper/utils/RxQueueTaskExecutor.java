package com.mediatek.blenativewrapper.utils;

import android.util.Pair;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import timber.log.Timber;

public class RxQueueTaskExecutor {
  private final ConcurrentLinkedQueue<Pair<Completable, CompletableEmitter>> actions2 = new ConcurrentLinkedQueue<>();
  private final AtomicBoolean isRunning = new AtomicBoolean(false);
  private final AtomicBoolean isReady = new AtomicBoolean(true);
  private final AtomicBoolean isOnError = new AtomicBoolean(false);
  private Throwable error = null;

  public Completable queue(Completable completable) {
    return Completable.create(e -> {
      actions2.add(new Pair<>(completable, e));
      runActionList();
    });
  }

  private void runActionList() {
    if (isReady.get() && isRunning.compareAndSet(false, true)) {
      Pair<Completable, CompletableEmitter> pair = actions2.poll();
      if (pair != null) {
        if(pair.second.isDisposed()){
          isRunning.set(false);
          runActionList();
          return;
        }
        if (isOnError.get()) {
          pair.second.onError(error);
          isRunning.set(false);
          runActionList();
        } else {
          pair.first.doOnTerminate(() -> {
            isRunning.set(false);
            runActionList();
          }).subscribe(() -> {
            if (!pair.second.isDisposed()) {
              pair.second.onComplete();
            }
          }, throwable -> {
            if (!pair.second.isDisposed()) {
              pair.second.onError(throwable);
            }
          });
        }
      } else {
        isRunning.set(false);
      }
    }
  }

  public void resume() {
    clearError();
    isReady.set(true);
    runActionList();
  }

  public void pause() {
    clearError();
    isReady.set(false);
  }

  private void clearError() {
    isOnError.set(false);
    error = null;
  }


  public void onError(Throwable e) {
    error = e;
    isOnError.set(true);
  }


}
