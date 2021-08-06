package com.mediatek.mt6381eco.rxbus;

import io.reactivex.Flowable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RxBus {
  private static final RxBus INSTANCE = new RxBus();
  private final FlowableProcessor<Object> mBus = PublishProcessor.create().toSerialized();
  private final Map<Class<?>,Object> mStickyEventMap = new ConcurrentHashMap<>();
  public static RxBus getInstance() {
    return INSTANCE;
  }
  public void post(Object event) {
    mBus.onNext(event);
  }
  public void postSticky(Object event){
    synchronized (mStickyEventMap) {
      mStickyEventMap.put(event.getClass(), event);
    }
    post(event);
  }

  public<T> Flowable<T> toFlowable(final Class<T> eventType){
   // final Object event = mStickyEventMap.get(eventType);
    return mBus.ofType(eventType).onBackpressureBuffer();
  }

    public <T> T removeStickyEvent(Class<T> eventType) {
    synchronized (mStickyEventMap) {
      return eventType.cast(mStickyEventMap.remove(eventType));
    }
  }

  public void removeAllStickyEvents() {
    synchronized (mStickyEventMap) {
      mStickyEventMap.clear();
    }
  }

}
