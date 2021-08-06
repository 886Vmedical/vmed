package com.mediatek.blenativewrapper;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Build;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mediatek.blenativewrapper.exceptions.ScanException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import timber.log.Timber;

public class BluetoothAdapterWrapper {

  private final BluetoothAdapter mBluetoothAdapter;
  private Callback mCallback;

  public BluetoothAdapterWrapper(BluetoothAdapter bluetoothAdapter) {
    mBluetoothAdapter = bluetoothAdapter;
  }

  public void startScan(@Nullable UUID[] serviceUuidFilteringList, @NonNull Callback callback)
      throws ScanException {
    if (null != mCallback) {
      Timber.e("null != mCallback");
      throw new ScanException("null != mCallback");
    }
    if (Build.VERSION_CODES.LOLLIPOP > Build.VERSION.SDK_INT) {
      startOldScan(serviceUuidFilteringList, callback);
    } else {
      startNewScan(serviceUuidFilteringList, callback);
    }
  }

  public void stopScan() {
    if (Build.VERSION_CODES.LOLLIPOP > Build.VERSION.SDK_INT) {
      stopOldScan();
    } else {
      stopNewScan();
    }
  }

  @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2) @SuppressWarnings("deprecation")
  private void startOldScan(@Nullable UUID[] serviceUuidFilteringList,
      @NonNull Callback callback) throws ScanException{
    Timber.i("startLeScan() call.");
    mCallback = callback;
    BluetoothAdapter.LeScanCallback leScanCallback = (device, rssi, scanRecord) -> {
      Timber.d("%s-%s",device.getAddress(), device.getName());
      if(mCallback != null) {
        mCallback.onLeScan(device, rssi, scanRecord);
      }
    };
    mCallback.mNativeCallback = leScanCallback;

    boolean ret;
    if (serviceUuidFilteringList == null) {
      ret = mBluetoothAdapter.startLeScan(leScanCallback);
    } else {
      ret = mBluetoothAdapter.startLeScan(serviceUuidFilteringList, leScanCallback);
    }
    if (!ret) {
      Timber.e("startLeScan() called. ret=false");
      throw new ScanException("startLeScan() called. ret=false");
    }
  }

  @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2) @SuppressWarnings("deprecation")
  private void stopOldScan() {
    Timber.i("stopLeScan() call.");
    if (mCallback != null) {
      mBluetoothAdapter.stopLeScan((BluetoothAdapter.LeScanCallback) mCallback.mNativeCallback);
      mCallback = null;
    }
    Timber.d("stopLeScan() called.");
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  private void startNewScan(@Nullable UUID[] serviceUuidFilteringList, Callback callback) throws
      ScanException{
    if (null == mBluetoothAdapter.getBluetoothLeScanner()) {
      throw new ScanException("getBluetoothLeScanner() is null");
    }
    List<ScanFilter> filters = null;
    if (null != serviceUuidFilteringList) {
      filters = new ArrayList<>();
      for (UUID serviceUuid : serviceUuidFilteringList) {
        ParcelUuid parcelUuid = new ParcelUuid(serviceUuid);
        ScanFilter filter = new ScanFilter.Builder().setServiceUuid(parcelUuid).build();
        filters.add(filter);
      }
    }
    ScanSettings settings =
        new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
    ScanCallback scanCallback = new ScanCallback() {


      @TargetApi(Build.VERSION_CODES.LOLLIPOP) @Override
      public void onScanResult(int callbackType, ScanResult result) {
        super.onScanResult(callbackType, result);
        Timber.d("%s-%s",result.getDevice().getAddress(),result.getDevice().getName());
        if(mCallback != null) {
          if (null == result.getScanRecord()) {
            mCallback.onLeScan(result.getDevice(), result.getRssi(), null);
          } else {
            mCallback.onLeScan(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes());
          }
        }
      }

      @Override public void onBatchScanResults(List<ScanResult> results) {
        super.onBatchScanResults(results);
      }

      @Override public void onScanFailed(int errorCode) {
        super.onScanFailed(errorCode);
        if(mCallback != null) {
          mCallback.onScanFail(errorCode);
        }
      }
    };
    Timber.i("startScan() call.");
    mBluetoothAdapter.getBluetoothLeScanner().startScan(filters, settings, scanCallback);
    Timber.d("startScan() called. ret=true");
    mCallback = callback;
    mCallback.mNativeCallback = scanCallback;
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP) private void stopNewScan() {
    if (null != mBluetoothAdapter.getBluetoothLeScanner()) {
      Timber.i("stopScan() call.");
      if(mCallback != null) {
        mBluetoothAdapter.getBluetoothLeScanner().stopScan((ScanCallback) mCallback.mNativeCallback);
      }
      mCallback = null;
      Timber.d("stopScan() called.");
    } else {
      Timber.e("null == mBluetoothAdapter.getBluetoothLeScanner()");
      mCallback = null;
    }

  }

  public int getState() {
    return mBluetoothAdapter.getState();
  }

  public static abstract class Callback {
    private Object mNativeCallback;
    abstract void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord);
    abstract void onScanFail(int errorCode);
  }
}
