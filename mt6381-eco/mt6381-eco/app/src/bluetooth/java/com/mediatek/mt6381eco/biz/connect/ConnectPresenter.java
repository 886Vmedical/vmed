package com.mediatek.mt6381eco.biz.connect;

import com.mediatek.mt6381eco.biz.peripheral.DeviceInfo;
import com.mediatek.mt6381eco.biz.peripheral.IBlePeripheral;
import com.mediatek.mt6381eco.biz.peripheral.IPeripheral;
import com.mediatek.mt6381eco.db.EasyDao;
import com.mediatek.mt6381eco.db.entries.BondDevice;
import com.mediatek.mt6381eco.viewmodel.Resource;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import javax.inject.Inject;
import timber.log.Timber;

public class ConnectPresenter implements IPresenter {
  private static final int MIN_THROUGHPUT = 1024 * 4; // 4k
  private final EasyDao mEasyDao;
  private final ConnectViewModel mViewModel;
  private IPeripheral mPeripheral;
  private final CompositeDisposable mDisposables = new CompositeDisposable();

  @Inject ConnectPresenter(EasyDao easyDao, ConnectViewModel viewModel) {
    mEasyDao = easyDao;
    mViewModel = viewModel;
  }

  @Override public void requestConnect(String macAddress) {
    mViewModel.connection.postValue(Resource.loading(null));
    mViewModel.throughputWarning.postValue(null);
    mDisposables.add(mPeripheral.connect()
        .doOnComplete(() -> {
          BondDevice bondDevice = new BondDevice();
          bondDevice.macAddress = macAddress;
          bondDevice.name = ((IBlePeripheral) mPeripheral).getName();
          mEasyDao.save(bondDevice);
        })
        .concatWith(checkMobileSpeed())
        .subscribe(() -> mViewModel.connection.postValue(Resource.success(null)),
            throwable -> mViewModel.connection.postValue(Resource.error(throwable, null))));
  }

  private Completable checkMobileSpeed() {
    return Completable.defer(() -> requestSysInfo().doOnSuccess(deviceInfo -> {
      Timber.i("throughput (min:%d): %d ", MIN_THROUGHPUT, deviceInfo.getThroughput());
      int throughput = deviceInfo.getThroughput();
      if (throughput < MIN_THROUGHPUT) {
        mViewModel.throughputWarning.postValue(throughput);
      }
    }).toCompletable());
  }

  @Override public void attach(IPeripheral service) {
    mPeripheral = service;
  }

  private Single<DeviceInfo> requestSysInfo() {
    return mPeripheral.readDeviceInfo();
  }

  @Override public void destroy() {
    mDisposables.clear();
  }
}
