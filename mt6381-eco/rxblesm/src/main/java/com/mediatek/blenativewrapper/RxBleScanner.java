package com.mediatek.blenativewrapper;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import com.mediatek.blenativewrapper.exceptions.ScanException;

public class RxBleScanner {
  private final AtomicBoolean mIsScanning = new AtomicBoolean(false);
  //make to public by krestin
  public final BluetoothAdapterWrapper mBluetoothAdapterWrapper;
  private final Context mContext;

  public RxBleScanner(Context context,BluetoothAdapterWrapper bluetoothAdapterWrapper){
    mBluetoothAdapterWrapper = bluetoothAdapterWrapper;
    mContext = context;
  }

  public Flowable<DiscoverPeripheral> scan(UUID[] serviceUuidFilteringList, int timeoutInSecond) {
    Flowable<DiscoverPeripheral> flowable = Flowable.create(e -> {
      if (BluetoothAdapter.STATE_ON != mBluetoothAdapterWrapper.getState()) {
        e.onError(new ScanException("Bluetooth is Off"));
        return;
      }
      if (!mIsScanning.compareAndSet(false, true)) {
        e.onError(new ScanException("Already Scanning"));
        return;
      }

      IntentFilter intentFilter = new IntentFilter();
      intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
      final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override public void onReceive(@NonNull Context context, @NonNull Intent intent) {
          String action = intent.getAction();
          if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
            e.onComplete();
          }
        }
      };
      mContext.registerReceiver(receiver, intentFilter);
      e.setCancellable(() -> {
        mBluetoothAdapterWrapper.stopScan();
        mIsScanning.set(false);
        mContext.unregisterReceiver(receiver);
      });



      final ConcurrentHashMap<String, DiscoverPeripheral> mLastDiscoverPeripherals =
          new ConcurrentHashMap<>();
      mBluetoothAdapterWrapper.startScan(serviceUuidFilteringList,
          new BluetoothAdapterWrapper.Callback() {
            @Override void onLeScan(BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord) {
              final DiscoverPeripheral discoverPeripheral;
              if (mLastDiscoverPeripherals.containsKey(bluetoothDevice.getAddress())) {
                discoverPeripheral = mLastDiscoverPeripherals.get(bluetoothDevice.getAddress());
                discoverPeripheral.update(rssi, scanRecord);
              } else {
                discoverPeripheral = new DiscoverPeripheral(bluetoothDevice, rssi, scanRecord);
                mLastDiscoverPeripherals.put(discoverPeripheral.getAddress(), discoverPeripheral);
              }
              e.onNext(discoverPeripheral);
            }

            @Override void onScanFail(int errorCode) {
              e.onError(new ScanException("errorCode:" + errorCode));
            }
          });
    }, BackpressureStrategy.BUFFER);
    if (timeoutInSecond >0) {
      flowable = flowable.timeout(timeoutInSecond, TimeUnit.SECONDS).onErrorResumeNext(throwable -> {
        if (throwable instanceof TimeoutException) {
          return Flowable.empty();
        }
        return Flowable.error(throwable);
      });
    }
    return flowable.doOnTerminate(() -> mIsScanning.set(false));
  }

  public boolean isScanning(){
    return mIsScanning.get();
  }


}
