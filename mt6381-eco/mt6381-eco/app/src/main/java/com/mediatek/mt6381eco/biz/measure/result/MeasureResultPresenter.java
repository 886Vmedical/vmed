package com.mediatek.mt6381eco.biz.measure.result;

import com.mediatek.mt6381eco.network.OAuthHelper;
import io.reactivex.Completable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import javax.inject.Inject;

public class MeasureResultPresenter implements MeasureResultContract.Presenter {
  private final MeasureResultContract.View mView;
  private final OAuthHelper mAuthHelper;

  private final CompositeDisposable mDisposables = new CompositeDisposable();

  @Inject MeasureResultPresenter(MeasureResultContract.View view, OAuthHelper authHelper) {
    mView = view;
    mAuthHelper = authHelper;
  }

  @Override public void destroy() {
    mDisposables.clear();
  }

  @Override public Completable upgrade() {
    return mAuthHelper.upgrade().subscribeOn(Schedulers.io());
  }
}
