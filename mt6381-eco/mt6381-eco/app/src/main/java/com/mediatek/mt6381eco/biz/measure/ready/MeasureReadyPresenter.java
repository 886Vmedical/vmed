package com.mediatek.mt6381eco.biz.measure.ready;

import com.mediatek.mt6381eco.biz.peripheral.IPeripheral;
import com.mediatek.mt6381eco.exceptions.ConnectionLostException;
import com.mediatek.mt6381eco.exceptions.ThroughputException;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import timber.log.Timber;

public class MeasureReadyPresenter implements MeasureReadyContract.Presenter {
  private static final int SAMPLE_INTERVAL = 500;
  private static final int SAMPLE_COUNT = 10;
  private static final int DOWN_SAMPLE_THROUGHPUT = 1024 * 3;//3k
  private static final int ALLOW_MIN_THROUGHPUT = 1024;//1k
  private final CompositeDisposable mDisposables = new CompositeDisposable();
  private MeasureReadyContract.View mView;
  private IPeripheral mPeripheral;
  private long mThroughput;

  @Inject MeasureReadyPresenter() {
  }

  @Override public void setView(MeasureReadyContract.View view) {
    mView = view;
  }

  @Override public void destroy() {
    mDisposables.clear();
  }

  @Override public void attach(IPeripheral peripheral) {
    mPeripheral = peripheral;
    mDisposables.add(mPeripheral.startThroughputTest()
        .concatWith(sampleThroughput())
        .concatWith(mPeripheral.stopMeasure())
        .subscribe(this::postThroughput,
            throwable -> mView.showError(new ThroughputException(throwable))));
    subscribeState();
  }

  @Override public void stopMeasure() {
    mDisposables.add(mPeripheral.stopMeasure()
        .onErrorComplete()
        .subscribe(() -> Timber.i("stop_test_throughput"), Timber::w));
  }

  private void postThroughput() {
    Timber.d("throughput = %d", mThroughput);
    if(mThroughput < ALLOW_MIN_THROUGHPUT){
      mView.alterThroughput(mThroughput, ALLOW_MIN_THROUGHPUT);
    }else {
      mView.navMeasurePage(mThroughput < DOWN_SAMPLE_THROUGHPUT);
    }
  }

  private void subscribeState() {
    mDisposables.add(Flowable.just(mPeripheral.getConnectionState())
        .mergeWith(mPeripheral.onConnectionChange())
        .filter(state -> state != IPeripheral.STATE_CONNECTED)
        .subscribe(integer -> mView.showError(new ConnectionLostException())));
  }

  private Completable sampleThroughput() {
    return Flowable.defer(() -> {
      mThroughput = Long.MAX_VALUE;
      return Flowable.interval(SAMPLE_INTERVAL, TimeUnit.MILLISECONDS).doOnNext(count -> {
        if (count > 1) {
          mThroughput = Math.min(mPeripheral.getThroughput(), mThroughput);
          Timber.d("throughput:%d", mThroughput);
        }
      }).take(SAMPLE_COUNT);
    }).ignoreElements();
  }
}