package com.mediatek.mt6381eco.biz.connect;

import android.arch.lifecycle.MutableLiveData;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import butterknife.BindView;
import butterknife.OnClick;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mediatek.blenativewrapper.BluetoothAdapterWrapper;
import com.mediatek.blenativewrapper.BluetoothDeviceWrapper;
import com.mediatek.blenativewrapper.DiscoverPeripheral;
import com.mediatek.blenativewrapper.RxBleScanner;
import com.mediatek.mt6381.ble.GattUUID;
import com.mediatek.mt6381eco.R;
import com.mediatek.mt6381eco.biz.peripheral.IPeripheral;
import com.mediatek.mt6381eco.biz.peripheral.PeripheralService;
import com.mediatek.mt6381eco.biz.peripheral.ServiceStartedEvent;
import com.mediatek.mt6381eco.dagger.Injectable;
import com.mediatek.mt6381eco.rxbus.RxBus;
import com.mediatek.mt6381eco.ui.BaseFragment;
import com.mediatek.mt6381eco.ui.data.IntentResult;
import com.mediatek.mt6381eco.ui.exceptions.ActivityIntentActionException;
import com.mediatek.mt6381eco.utils.ServiceBinding;
import com.mediatek.mt6381eco.viewmodel.Resource;
import com.mediatek.mt6381eco.viewmodel.Status;
import io.reactivex.Completable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.PublishSubject;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import timber.log.Timber;

public class ConnectFragment extends BaseFragment implements Injectable {
  public static final int REQUEST_CUSTOM = 1;
  private static final String PX_MT6381 = "MT6381";
  private static final String MAC_ADDRESS_PX1 = "00:0B:57";
  private static final String MAC_ADDRESS_PX2 = "90:FD:9F";
  public final MutableLiveData<Resource<ArrayList<DiscoverPeripheral>>> peripheralList =
      new MutableLiveData<>();

  ConcurrentHashMap<String, DiscoverPeripheral> mLastDiscoverPeripherals =
      new ConcurrentHashMap<>();
  @BindView(R.id.list_view) ListView mListView;
  @BindView(R.id.btn_connect) Button mBtnConnect;
  @Inject IPresenter mPresenter;
  @Inject ConnectViewModel mViewModel;
  private PeripheralAdapter mAdapter;
  private final CompositeDisposable mDisposables = new CompositeDisposable();
  private final PublishSubject<IntentResult> mSubjectIntentResult = PublishSubject.create();

  private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
    @Override public void onReceive(Context context, Intent intent) {
      peripheralList.setValue(Resource.success(new ArrayList<>()));
    }
  };
  private RxBleScanner mRxBleScanner;
  private ServiceBinding.Unbind mServiceUnBinder;

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
    getActivity().getApplication().registerReceiver(mReceiver, intentFilter);
    mAdapter = new PeripheralAdapter();
    mRxBleScanner = new RxBleScanner(getActivity().getApplicationContext(),
        new BluetoothAdapterWrapper(BluetoothAdapter.getDefaultAdapter()));
    checkAndScan();
    mServiceUnBinder =
        ServiceBinding.bindService(this, PeripheralService.class,
            service -> mPresenter.attach((IPeripheral) service));
  }

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.activity_connect, container, false);
  }

  @Override protected void initView(Bundle savedInstanceState) {

    mListView.setAdapter(mAdapter);
    peripheralList.observe(this, resource -> {
      if (resource.status == Status.ERROR) {
        showError(resource.throwable);
        mListView.clearChoices();
      }
      mAdapter.setList(resource.data);
      mBtnConnect.setEnabled(mListView.getCheckedItemPosition() > -1
          && mListView.getCheckedItemPosition() < mAdapter.getCount());
    });

    mViewModel.connection.observe(this, resource -> {
      switch (resource.status){
        case LOADING:{
          startLoading(getString(R.string.connecting));
          break;
        }
        case SUCCESS:{
          stopLoading();
          if(mViewModel.throughputWarning.getValue() != null){
            showThroughputWarning(mViewModel.throughputWarning.getValue());
          }else {
            getActivity().finish();
          }
          break;
        }
        case ERROR:{
          stopLoading();
          showError(resource.throwable);
          break;
        }
      }
    });
    mListView.setOnItemClickListener((adapterView, view, i, l) -> {
      //krestin add to stop device scan when user selected start
      if(null != mRxBleScanner.mBluetoothAdapterWrapper) {
        mRxBleScanner.mBluetoothAdapterWrapper.stopScan();
      }
      //krestin add to stop device scan when user selected end
      mListView.setItemChecked(i, true);
      mBtnConnect.setEnabled(mListView.getCheckedItemPosition() > -1);
    });
  }

  private void showThroughputWarning(int throughputBps) {
    new MaterialDialog.Builder(getActivity()).title(R.string.throughput)
        .content(R.string.throughput_warning_formatter, throughputBps / 1024f)
        .positiveText(R.string.ok)
        .dismissListener(dialogInterface ->getActivity().finish())
        .show();
  }

  @Override public void onDestroy() {
    mDisposables.clear();
    super.onDestroy();
    getActivity().getApplication().unregisterReceiver(mReceiver);
    mServiceUnBinder.unbind();
    mPresenter.destroy();
  }

  private void removeAllBond() {
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()) {
      if (device.getName().startsWith(PX_MT6381) || device.getAddress()
          .toUpperCase()
          .startsWith(MAC_ADDRESS_PX1) || device.getAddress()
          .toUpperCase()
          .startsWith(MAC_ADDRESS_PX2)) {
        new BluetoothDeviceWrapper(getActivity().getApplication(), device).removeBond();
      }
    }
  }

  private Completable enableBt() {
    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    return Completable.defer(() -> {
      BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
      if (bluetoothAdapter.isEnabled()) {
        return Completable.complete();
      } else {
        startActivityForResult(intent, REQUEST_CUSTOM);
        return mSubjectIntentResult.firstOrError().flatMapCompletable(intentResult -> {
          if (bluetoothAdapter.isEnabled()) return Completable.complete();
          return Completable.error(new ActivityIntentActionException(intent));
        });
      }
    });
  }

  @OnClick(R.id.txt_refresh) void onTxtRefreshClick() {
    mDisposables.clear();
    mListView.clearChoices();
    mBtnConnect.setEnabled(false);
    checkAndScan();
  }

  @OnClick(R.id.btn_skip) void onBtnSkipClick() {
    getActivity().finish();
  }
  @OnClick(R.id.btn_connect) void onBtnConnectClick() {
    doConnect();
  }

  private void checkAndScan() {
    mDisposables.add(
        enableBt().doOnComplete(this::removeAllBond).subscribe(this::startScan, this::showError));
  }

  private void startScan() {
    scan(new UUID[]{ GattUUID.Service.System.getUuid(), GattUUID.Service.Data.getUuid()});
  }

  public void scan(UUID[] serviceUuidFilteringList) {
    mLastDiscoverPeripherals.clear();
    mDisposables.add(
        mRxBleScanner.scan(serviceUuidFilteringList, 0)
            .doOnNext(discoverPeripheral -> {
              if (!mLastDiscoverPeripherals.containsKey(discoverPeripheral.getAddress())) {
                mLastDiscoverPeripherals.put(discoverPeripheral.getAddress(), discoverPeripheral);
              }
            })
            .doOnSubscribe(subscription -> peripheralList.postValue(
                Resource.loading(new ArrayList<>(mLastDiscoverPeripherals.values()))))
            .subscribe(discoverPeripherals -> peripheralList.postValue(
                Resource.loading(new ArrayList<>(mLastDiscoverPeripherals.values()))),
                throwable -> peripheralList.postValue(Resource.error(throwable, new ArrayList<>())),
                () -> peripheralList.postValue(Resource.success(new ArrayList<>()))));
  }

  @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
    mSubjectIntentResult.onNext(new IntentResult(resultCode, data));
  }

  private void doConnect() {
    mDisposables.clear();
    int position = mListView.getCheckedItemPosition();
    if (position > -1) {
      DiscoverPeripheral peripheral = mAdapter.getItem(position);
      Timber.i("connect to :%s-%s", peripheral.getLocalName(), TextUtils.join(",", peripheral.getAdvertiseDataList()));
      mDisposables.add(RxBus.getInstance()
          .toFlowable(ServiceStartedEvent.class)
          .take(1)
          .timeout(5, TimeUnit.SECONDS)
          .ignoreElements()
          .onErrorComplete()
          .subscribe(() -> mPresenter.requestConnect(peripheral.getAddress())));
      getActivity().getApplication()
          .startService(
              new Intent(getActivity().getApplication(), PeripheralService.class).putExtra(
                  PeripheralService.INTENT_PERIPHERAL, peripheral));
    }
  }
}
