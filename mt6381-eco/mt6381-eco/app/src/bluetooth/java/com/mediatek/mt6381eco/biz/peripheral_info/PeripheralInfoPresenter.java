package com.mediatek.mt6381eco.biz.peripheral_info;

import android.util.Log;

import com.mediatek.mt6381eco.biz.peripheral.DeviceInfo;
import com.mediatek.mt6381eco.biz.peripheral.IPeripheral;
import com.mediatek.mt6381eco.dagger.IAppContext;
import com.mediatek.mt6381eco.db.EasyDao;
import com.mediatek.mt6381eco.db.entries.BondDevice;
import com.mediatek.mt6381eco.network.ApiService;
import com.mediatek.mt6381eco.network.model.FirmwareResponse;
import com.mediatek.mt6381eco.viewmodel.Resource;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import javax.inject.Inject;
import okhttp3.ResponseBody;
import timber.log.Timber;

public class PeripheralInfoPresenter implements PeripheralInfoContract.Presenter {
  private static final String FOTA_TYPE_APP = "app";
  private static final String FOTA_TYPE_STACK = "stack";

  private final PeripheralInfoViewModel mViewModel;
  private final EasyDao mEasyDao;
  private final BondDevice mBondDevice;
  private final RxFota mRxFota;
  private final ApiService mApiService;
  private final IAppContext mAppContext;
  private final CompositeDisposable mDisposables = new CompositeDisposable();
  private IPeripheral mPeripheral;
  private Disposable mDownloadDisposable;
  private DeviceInfo mDeviceInfo;
  private FirmwareResponse mRemoteFirmware;

  @Inject PeripheralInfoPresenter(PeripheralInfoViewModel viewModel, EasyDao easyDao, RxFota rxFota,
      ApiService apiService, IAppContext appContext) {
    mViewModel = viewModel;
    mEasyDao = easyDao;
    mApiService = apiService;
    mAppContext = appContext;
    mBondDevice = mEasyDao.find(BondDevice.class);
    mRxFota = rxFota;
    if (mBondDevice != null) {
      mViewModel.deviceName.setValue(mBondDevice.name);
      rxFota.setMacAddress(mBondDevice.macAddress);
    }
    mDeviceInfo = new DeviceInfo(1000, 33, "1.0");
    getLatestVersion();
  }

  @Override public void destroy() {
    mDisposables.clear();
    mRxFota.destroy();
  }

  @Override public void attach(IPeripheral peripheral) {

    mDisposables.add(peripheral.readDeviceInfo()
        .doOnSubscribe(disposable -> mViewModel.info.setValue(Resource.loading(null)))
        .subscribe(deviceInfo -> {
          mDeviceInfo = deviceInfo;
          PeripheralInfoViewModel.PeripheralInfo peripheralInfo =
              new PeripheralInfoViewModel.PeripheralInfo();
          peripheralInfo.power = deviceInfo.getBattery();
          Log.d("PeripheralInfoPresenter","peripheralInfo.power: " + peripheralInfo.power);
          peripheralInfo.synced = new Date();
          //todo by herman
          peripheralInfo.version = deviceInfo.getFirmwareVersion();
          Log.d("PeripheralInfoPresenter","peripheralInfo.version: " + peripheralInfo.version);
          mViewModel.info.postValue(Resource.success(peripheralInfo));
          //delete by herman
          //checkHasNewFirmware();
        }, throwable -> {
          mViewModel.info.postValue(Resource.error(throwable, null));
          mViewModel.newFirmware.postValue(Resource.error(throwable, null));
        }));
    mPeripheral = peripheral;
  }

  private synchronized void checkHasNewFirmware() {
    if (mRemoteFirmware != null && mRemoteFirmware.version != null && mDeviceInfo != null) {
      if (mRemoteFirmware.version.compareTo(mDeviceInfo.getFirmwareVersion()) >= 0) {
        mViewModel.newFirmware.postValue(Resource.success(mRemoteFirmware.version));
      } else {
        mViewModel.newFirmware.postValue(Resource.success(null));
      }
    }
  }

  @Override public PeripheralInfoViewModel getViewModel() {
    return mViewModel;
  }

  @Override public void disconnect() {
    mPeripheral.disconnect();
  }

  @Override public void changeName(String name) {
    mViewModel.deviceName.postValue(name);
    mBondDevice.name = name;
    mEasyDao.save(mBondDevice);
  }

  @Override public void startDownload() {
    mViewModel.downloadProgress.postValue(Resource.loading(0f));
    mDownloadDisposable = Flowable.fromArray(mRemoteFirmware.files)
        .flatMap(fwFile -> mApiService.getStream(fwFile.filePath)
            .map(responseBody -> new FilePair(responseBody, fwFile.fileName))
            .toFlowable())
        .toList()
        .flatMapObservable(filePairs -> {
          int sum = 0;
          for (int i = 0; i < filePairs.size(); ++i) {
            ResponseBody responseBody = filePairs.get(i).responseBody;
            sum += responseBody.contentLength();
          }
          final long length = sum;
          return Observable.fromIterable(filePairs).flatMap(filePair -> {
            File outFile = getFotaFile(filePair.name);
            return rxDownload(filePair.responseBody, outFile);
          }).scan((a, b) -> a + b).doOnNext(downloaded -> {
            Timber.d("%d/%d", downloaded, length);
            mViewModel.downloadProgress.postValue(Resource.loading(downloaded * 100f / length));
          });
        })
        .subscribeOn(Schedulers.io())
        .ignoreElements()
        .doOnDispose(() -> mViewModel.downloadProgress.postValue(Resource.cancel(null)))
        .subscribe(() -> mViewModel.downloadProgress.postValue(Resource.success(100f)),
            throwable -> mViewModel.downloadProgress.postValue(Resource.error(throwable, null)));
    mDisposables.add(mDownloadDisposable);
  }

  private File getFotaFile(String type) {
    return new File(mAppContext.getDownloadDir(),
        String.format("fota/%s/%s", mRemoteFirmware.version, type));
  }

  private Observable<Integer> rxDownload(ResponseBody responseBody, File outFile) {
    return Observable.create(e -> {
      int read;
      byte[] buffer = new byte[1024];
      InputStream input = responseBody.byteStream();
      if (!outFile.getParentFile().exists()) {
        outFile.getParentFile().mkdirs();
      }
      OutputStream outputStream = new FileOutputStream(outFile);
      while ((read = input.read(buffer)) > -1) {
        outputStream.write(buffer, 0, read);
        e.onNext(read);
      }
      outputStream.close();
      e.onComplete();
    });
  }

  @Override public void cancelDownload() {
    if (mDownloadDisposable != null) {
      mDownloadDisposable.dispose();
    }
  }

  @Override public void startInstall() {
    try {
      mPeripheral.disconnect();
      InputStream stackInputStream = new FileInputStream(getFotaFile(FOTA_TYPE_STACK));
      InputStream appInputStream = new FileInputStream(getFotaFile(FOTA_TYPE_APP));
      mRxFota.doFota(stackInputStream, appInputStream)
          .doOnSubscribe(subscription -> mViewModel.fotaProgress.postValue(Resource.loading(0f)))
          .doOnTerminate(() -> {
            stackInputStream.close();
            appInputStream.close();
          })
          .subscribe(progress -> mViewModel.fotaProgress.postValue(Resource.loading(progress)),
              throwable -> mViewModel.fotaProgress.postValue(Resource.error(throwable, null)),
              () -> mViewModel.fotaProgress.postValue(Resource.success(100f)));
    } catch (IOException e) {
      Timber.e(e);
      mViewModel.fotaProgress.postValue(Resource.error(e, null));
    }
  }

  private void getLatestVersion() {
    mDisposables.add(mApiService.getLatestFirmware()
        .doOnSubscribe(disposable -> mViewModel.newFirmware.postValue(Resource.loading("")))
        .subscribeOn(Schedulers.io())
        .subscribe(firmwareResponse -> {
          mRemoteFirmware = firmwareResponse;
          checkHasNewFirmware();
        }, throwable -> mViewModel.newFirmware.postValue(Resource.error(throwable, null))));
  }

  private static class FilePair {
    ResponseBody responseBody;
    String name;

    public FilePair(ResponseBody responseBody, String name) {
      this.responseBody = responseBody;
      this.name = name;
    }
  }
}
