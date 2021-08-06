package com.mediatek.mt6381eco.biz.peripheral_info;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import com.mediatek.blenativewrapper.BLEDataReceiver;
import com.mediatek.blenativewrapper.BlePeripheralSettings;
import com.mediatek.blenativewrapper.BluetoothAdapterWrapper;
import com.mediatek.blenativewrapper.DiscoverPeripheral;
import com.mediatek.blenativewrapper.GattConnectPriority;
import com.mediatek.blenativewrapper.RxBlePeripheral;
import com.mediatek.blenativewrapper.RxBleScanner;
import com.mediatek.blenativewrapper.utils.MeasureSpeed;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import timber.log.Timber;

public class RxFota {
  private static final UUID UUID_OTA_CONTROL =
      UUID.fromString("f7bf3564-fb6d-4e53-88a4-5e37e0326063");
  private static final UUID UUID_OTA_DATA = UUID.fromString("984227f3-34fc-4045-a5d0-2c581f81a153");
  private static final long TIME_OUT_SCAN = 20;
  private final RxBlePeripheral mRxBlePeripheral;
  private final RxBleScanner mRxBleScanner;
  MeasureSpeed mMeasureSpeed = new MeasureSpeed(2000, 50);
  private String mMacAddress;
  private DiscoverPeripheral mDiscoverPeripheral;

  @Inject RxFota(Application application) {
    mRxBleScanner = new RxBleScanner(application,
        new BluetoothAdapterWrapper(BluetoothAdapter.getDefaultAdapter()));
    Bundle bundle = new Bundle();
    bundle.putInt(BlePeripheralSettings.Key.MtuSize.name(), 512);
    bundle.putBoolean(BlePeripheralSettings.Key.UseCreateBond.name(), false);
    bundle.putBoolean(BlePeripheralSettings.Key.UseRemoveBond.name(), false);
    mRxBlePeripheral = new RxBlePeripheral(application, bundle, new BLEDataReceiver() {

      @Override public void reset() {

      }

      @Override public void receive(UUID uuid, byte[] data, int len) {

      }

      @Override public void destroy() {

      }
    });
  }

  public Flowable<Float> doFota(InputStream stackInputStream, InputStream appInputStream) {
    mMeasureSpeed.reset();
    mDiscoverPeripheral = null;
    return Flowable.defer(() -> {
      int bytesCount = stackInputStream.available() + appInputStream.available();
      int minStep = (bytesCount / 100);
      final int totalStep = bytesCount + minStep * 5;
      return Single.just(0)
          .concatWith(mRxBlePeripheral.disconnect().onErrorComplete().toSingleDefault(0))
          .concatWith(scan().toSingleDefault(minStep).toFlowable())
          .concatWith(connect().toSingleDefault(minStep).toFlowable())
          .concatWith(toOtaMode().toSingleDefault(minStep).toFlowable())
          .concatWith(requestConnectionPriority().toFlowable())
          .concatWith(otaBegin().toFlowable())
          .concatWith(otaUploadData(stackInputStream))
          .concatWith(otaEnd().toFlowable())
          .concatWith(disconnect().toFlowable())
          .concatWith(scan().toSingleDefault(minStep).toFlowable())
          .concatWith(connect().toSingleDefault(minStep).toFlowable())
          .concatWith(requestConnectionPriority().toFlowable())
          .concatWith(otaBegin().toFlowable())
          .concatWith(otaUploadData(appInputStream))
          .concatWith(otaEnd().toFlowable())
          .scan((a, b) -> a + b)
          .doOnNext(integer -> Timber.d("fota_bps:%d", mMeasureSpeed.bps()))
          .map(step -> step * 100f / totalStep)
          .doOnError(Timber::e);
    });
  }

  void setMacAddress(String macAddress) {
    mMacAddress = macAddress;
  }

  public void destroy() {
    mRxBlePeripheral.destroy();
  }

  private Completable scan() {
    return mRxBleScanner.scan(null, 0)
        .doOnNext(discoverPeripheral -> Timber.d("%s-%s", discoverPeripheral.getLocalName(),
            discoverPeripheral.getAddress()))
        .filter(discoverPeripheral -> discoverPeripheral.getAddress().equalsIgnoreCase(mMacAddress))
        .firstElement()
        .timeout(TIME_OUT_SCAN, TimeUnit.SECONDS)
        .doOnSuccess(discoverPeripheral -> {
          mDiscoverPeripheral = discoverPeripheral;
          Timber.i("fota_device_name:%s -%s", mDiscoverPeripheral.getLocalName(),
              mDiscoverPeripheral.toString());
        })
        .ignoreElement();
  }

  private Completable toOtaMode() {
    return Completable.defer(() -> {
      if (mRxBlePeripheral.getCharacteristic(UUID_OTA_DATA) != null) {
        return Completable.complete();
      }
      return otaBegin().concatWith(disconnect()).concatWith(scan()).concatWith(connect());
    });
  }

  private Completable requestConnectionPriority() {
    return mRxBlePeripheral.requestConnectionPriority(GattConnectPriority.CONNECTION_PRIORITY_HIGH)
        .onErrorComplete();
  }

  private Completable connect() {
    return Completable.defer(() -> {
      Timber.i("connect %s -%s -%s", mDiscoverPeripheral.getAddress(),
          mDiscoverPeripheral.getLocalName(), mDiscoverPeripheral.toString());
      return mRxBlePeripheral.changePeripheral(mDiscoverPeripheral)
          .concatWith(mRxBlePeripheral.connect());
    });
  }

  private Completable otaBegin() {
    return Completable.defer(() -> {
      Timber.i("otaBegin --start");
      return mRxBlePeripheral.writeCharacteristic(
          mRxBlePeripheral.getCharacteristic(UUID_OTA_CONTROL), false, new byte[] { 0x00 });
    }).doOnComplete(() -> Timber.i("otaBegin-- completed")).onErrorComplete();
  }

  private Completable otaEnd() {
    return Completable.defer(() -> {
      Timber.i("otaEnd-start");
      return mRxBlePeripheral.writeCharacteristic(
          mRxBlePeripheral.getCharacteristic(UUID_OTA_CONTROL), false, new byte[] { 0x03 });
    }).doOnComplete(() -> Timber.i("otaEnd -- completed")).onErrorComplete();
  }

  private Completable disconnect() {
    return Completable.defer(mRxBlePeripheral::disconnect).doOnError(Timber::e).onErrorComplete();
  }

  private Flowable<Integer> otaUploadData(InputStream inputStream) {
    return Flowable.defer(() -> {
      Timber.i("otaUploadData-start");
      return Flowable.fromIterable(toIterable(inputStream, mRxBlePeripheral.getMtuSize() - 3));
    }).concatMap(bytes -> {
      mMeasureSpeed.receive(bytes.length);
      return mRxBlePeripheral.writeCharacteristic(mRxBlePeripheral.getCharacteristic(UUID_OTA_DATA),
          false, bytes).toSingleDefault(bytes.length).toFlowable();
    });
  }

  private Iterable<byte[]> toIterable(InputStream inputStream, int packageSize) {
    return () -> new Iterator<byte[]>() {
      boolean isLoaded = false;
      byte[] buffer = new byte[packageSize];
      byte[] mCurrent = null;

      @Override public boolean hasNext() {
        if (!isLoaded) {
          try {
            loadItem();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
        return mCurrent != null;
      }

      private void loadItem() throws IOException {
        mCurrent = null;
        int len = 0;
        while (len == 0) {
          len = inputStream.read(buffer);
        }
        if (len > 0) {
          if (len < buffer.length) {
            byte[] temp = new byte[len % 3 == 0 ? len : len + (4 - len % 4)];
            Arrays.fill(temp, (byte) 0xff);
            System.arraycopy(buffer, 0, temp, 0, len);
            buffer = temp;
          }
          mCurrent = buffer;
        }
        isLoaded = true;
      }

      @Override public byte[] next() {
        isLoaded = false;
        return mCurrent;
      }
    };
  }
}
