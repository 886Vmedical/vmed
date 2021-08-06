package com.mediatek.blenativewrapper;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.os.Bundle;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mediatek.blenativewrapper.exceptions.CommunicateException;
import com.mediatek.blenativewrapper.exceptions.ConnectionLostException;
import com.mediatek.blenativewrapper.exceptions.GattStatusException;
import com.mediatek.blenativewrapper.utils.RxQueueTaskExecutor;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Predicate;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import java.util.UUID;
import timber.log.Timber;

public class RxBlePeripheral {
  protected final Scheduler mWorkScheduler;
  private final BlePeripheral mBlePeripheral;
  private final BLEDataReceiver mDataParser;
  private final FlowableProcessor<Object> mSubject = PublishProcessor.create().toSerialized();
  private final RxQueueTaskExecutor mRxQueueTaskExecutor = new RxQueueTaskExecutor();
  private final BlePeripheral.ActionReceiver mActionReceiver = new BlePeripheral.ActionReceiver() {
    @Override public void didDisconnection(@NonNull String address) {
      mSubject.onNext(getStateInfo());
    }

    @Override public void onCharacteristicChanged(@NonNull UUID uuid, byte[] data, int length) {
      RxBlePeripheral.this.onCharacteristicChanged(uuid, data, length);
    }
  };
  private final StateInfo.StateMonitor mStateMonitor = new StateInfo.StateMonitor() {
    @Override public void onBondStateChanged(@NonNull StateInfo.BondState bondState) {
      mSubject.onNext(getStateInfo());
    }

    @Override public void onAclConnectionStateChanged(
        @NonNull StateInfo.AclConnectionState aclConnectionState) {
      mSubject.onNext(getStateInfo());
    }

    @Override public void onGattConnectionStateChanged(
        @NonNull StateInfo.GattConnectionState gattConnectionState) {
      mSubject.onNext(getStateInfo());
    }

    @Override
    public void onConnectionStateChanged(@NonNull StateInfo.ConnectionState connectionState) {
      Timber.i("onConnectionStateChanged:%s", connectionState.name());
      RxBlePeripheral.this.onConnectionStateChanged(connectionState);
      if (connectionState == StateInfo.ConnectionState.Connected) {
        mRxQueueTaskExecutor.pause();
      }
      mSubject.onNext(getStateInfo());
    }

    @Override public void onDetailedStateChanged(@NonNull StateInfo.DetailedState detailedState) {
      Timber.i("onDetailedStateChanged:%s", detailedState.name());
      if (detailedState == StateInfo.DetailedState.CommunicationReady) {
        mRxQueueTaskExecutor.resume();
      } else if(getStateInfo().isConnected()) {
        mRxQueueTaskExecutor.pause();
      }else {
        mRxQueueTaskExecutor.onError(new ConnectionLostException(detailedState.name()));
      }
      mSubject.onNext(getStateInfo());
    }
  };

  public RxBlePeripheral(Context context, Bundle setting,
      BLEDataReceiver dataParser) {
    HandlerThread handlerThread = new HandlerThread(getClass().getSimpleName());
    handlerThread.start();
    mBlePeripheral =
        new BlePeripheral(context,  setting, handlerThread.getLooper());
    mDataParser = dataParser;
    mWorkScheduler = AndroidSchedulers.from(handlerThread.getLooper());
  }

  protected void onCharacteristicChanged(UUID uuid, byte[] data, int length) {
    mDataParser.receive(uuid, data, length);
  }

  protected void onConnectionStateChanged(StateInfo.ConnectionState connectionState) {
    if (connectionState != StateInfo.ConnectionState.Connected) {
      mDataParser.reset();
    }
    mSubject.onNext(getStateInfo());
  }

  public StateInfo getStateInfo() {
    return mBlePeripheral.getStateInfo();
  }

  public Flowable<StateInfo> onStateChange() {
    return mSubject.ofType(StateInfo.class);
  }

  public Flowable<StateInfo.ConnectionState> onConnectionStateChange() {
    return onStateChange().map(StateInfo::getConnectionState).filter(new Predicate<StateInfo.ConnectionState>() {
      StateInfo.ConnectionState last = null;
      @Override
      public boolean test(@io.reactivex.annotations.NonNull StateInfo.ConnectionState state)
          throws Exception {
        return  last != (last = state);
      }
    });
  }

  public BluetoothGattCharacteristic getCharacteristic(String characteristicUUID) {
    return getCharacteristic(UUID.fromString(characteristicUUID));
  }

  public @Nullable BluetoothGattCharacteristic getCharacteristic(UUID characteristicUUID) {
    return mBlePeripheral.getCharacteristic(characteristicUUID);
  }

  protected Completable setNotificationEnabled(
      final BluetoothGattCharacteristic characteristic, boolean enable) {
    if(characteristic == null){
      return Completable.error(new CommunicateException("characteristic is null"));
    }
    return mRxQueueTaskExecutor.queue(Completable.create(
        e -> mBlePeripheral.setNotificationEnabled(characteristic, enable,
            new BlePeripheral.CommunicateListener() {
              @Override public void onComplete(int gattStatus) {
                if (GattStatusCode.GATT_SUCCESS == gattStatus) {
                  e.onComplete();
                } else {
                  e.onError(new CommunicateException("gattStatus:" + gattStatus));
                }
              }

              @Override public void onError(Throwable throwable) {
                e.onError(throwable);
              }
            })));
  }

  protected Completable readCharacteristic(@NonNull final UUID characteristicUuid) {
    return readCharacteristic(getCharacteristic(characteristicUuid));
  }

  protected Completable readCharacteristic(@NonNull final String characteristic) {
    return readCharacteristic(getCharacteristic(characteristic));
  }

  protected Completable readCharacteristic(
     final BluetoothGattCharacteristic characteristic) {
    if(characteristic == null){
      return Completable.error(new CommunicateException("characteristic is null"));
    }
    return mRxQueueTaskExecutor.queue(Completable.create(
        emitter -> mBlePeripheral.readCharacteristic(characteristic,
            new BlePeripheral.CommunicateListener() {
              @Override public void onComplete(int gattStatus) {
                if (GattStatusCode.GATT_SUCCESS == gattStatus) {
                  emitter.onComplete();
                } else {
                  emitter.onError(new GattStatusException("error gattStatus:"  + gattStatus, gattStatus));
                }
              }

              @Override public void onError(Throwable throwable) {
                emitter.onError(throwable);
              }
            }))).subscribeOn(mWorkScheduler);
  }

  protected Completable writeCharacteristic(
      @NonNull final BluetoothGattCharacteristic characteristic, byte[] data) {
    return writeCharacteristic(characteristic, false, data);
  }

  public int getMtuSize(){
    return mBlePeripheral.getMtuSize();
  }

  public Completable writeCharacteristic(
      final BluetoothGattCharacteristic characteristic, boolean noResponse, byte[] data) {
    if(characteristic == null){
      return Completable.error(new CommunicateException("characteristic is null"));
    }
    return mRxQueueTaskExecutor.queue(Completable.create(
        emitter ->{
          Timber.d("writeCharacteristic");
          mBlePeripheral.writeCharacteristic(characteristic, noResponse, data,
            new BlePeripheral.CommunicateListener() {
              @Override public void onComplete(int gattStatus) {
                if (GattStatusCode.GATT_SUCCESS == gattStatus) {
                  emitter.onComplete();
                } else {
                  emitter.onError(new GattStatusException("error gattStatus:"  + gattStatus, gattStatus));
                }
              }

              @Override public void onError(Throwable throwable) {
                emitter.onError(throwable);
              }
            });})).subscribeOn(mWorkScheduler);
  }

  public Completable connect() {
    return Completable.create(
        e -> mBlePeripheral.connect(mActionReceiver, new BlePeripheral.ActionCallbackListener() {
          @Override public void onComplete() {
            onConnectedCommunication();
            e.onComplete();
          }

          @Override public void onError(Throwable throwable) {
            e.onError(throwable);
          }
        }, mStateMonitor));
  }

  public String getAddress() {
    return mBlePeripheral.getAddress();
  }

  protected Completable beforeDisconnect() {
    return Completable.complete();
  }

  public Completable disconnect() {
    return beforeDisconnect().concatWith(Completable.create(
        e -> mBlePeripheral.disconnect(new BlePeripheral.ActionCallbackListener() {
          @Override public void onComplete() {
            e.onComplete();
          }

          @Override public void onError(Throwable throwable) {
            e.onError(throwable);
          }
        }))).doOnComplete(() -> Timber.i("Disconnect success"));
  }

  public Completable changePeripheral(DiscoverPeripheral discoverPeripheral) {
    Timber.i("changePeripheral:%s", discoverPeripheral.getAddress());
    return Completable.create(e -> mBlePeripheral.changeDevice(discoverPeripheral,
        new BlePeripheral.ActionCallbackListener() {
          @Override public void onComplete() {
            Timber.i("changePeripheral completed");
            e.onComplete();
          }

          @Override public void onError(Throwable throwable) {
            Timber.e(throwable, "changePeripheral error");
            e.onError(throwable);
          }
        }));
  }

  public void destroy() {
    disconnect().onErrorComplete().subscribe(mBlePeripheral::destroy);
    mDataParser.destroy();
  }

  protected void onConnectedCommunication() {

  }

 public Completable requestConnectionPriority(int connectionPriority) {
    return mRxQueueTaskExecutor.queue(Completable.create(
        emitter -> mBlePeripheral.requestConnectionPriority(connectionPriority,
            new BlePeripheral.ActionCallbackListener() {
              @Override public void onComplete() {
                emitter.onComplete();
              }

              @Override public void onError(Throwable throwable) {
                emitter.onError(throwable);
              }
            }))).subscribeOn(mWorkScheduler);
  }
}
