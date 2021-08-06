package com.mediatek.blenativewrapper.utils;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

public class RxUtils {
  public <T> Completable toCompletable(Flowable<T> flowable) {
    return Completable.create(e -> {
      Disposable disposable =
          flowable.subscribe(o -> Timber.d("skip on next"), e::onError, e::onComplete);
      e.setCancellable(disposable::dispose);
    });
  }

  public <T> Completable toCompletable(Single<T> single) {
    return Completable.create(e -> {
      Disposable disposable = single.subscribe(t -> e.onComplete(), e::onError);
      e.setCancellable(disposable::dispose);
    });
  }
}
