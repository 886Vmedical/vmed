package com.mediatek.mt6381eco.biz.peripheral_info;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.OnClick;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mediatek.mt6381eco.R;
import com.mediatek.mt6381eco.biz.home.ModelPowerBroadCastReceiver;
import com.mediatek.mt6381eco.biz.peripheral.IPeripheral;
import com.mediatek.mt6381eco.biz.peripheral.PeripheralService;
import com.mediatek.mt6381eco.dagger.Injectable;
import com.mediatek.mt6381eco.ui.BaseFragment;
import com.mediatek.mt6381eco.ui.ContainerActivity;
import com.mediatek.mt6381eco.ui.ContextUtils;
import com.mediatek.mt6381eco.ui.OnBackPressedListener;
import com.mediatek.mt6381eco.utils.MTextUtils;
import com.mediatek.mt6381eco.utils.ServiceBinding;
import com.mediatek.mt6381eco.viewmodel.Resource;
import com.mediatek.mt6381eco.viewmodel.Status;
import java.util.Locale;
import com.mediatek.mt6381.ble.MT6381SystemInfoParser;

import javax.inject.Inject;

public class PeripheralInfoFragment extends BaseFragment
    implements Injectable, OnBackPressedListener {
  private final int REQUEST_EDIT_NAME = 1;
  @Inject
  PeripheralInfoContract.Presenter mPresenter;
  @Inject PeripheralInfoViewModel mViewModel;
  @BindView(R.id.txt_device_name) TextView mTxtDeviceName;
  @BindView(R.id.txt_power) TextView mTxtPower;
  @BindView(R.id.txt_synced) TextView mTxtSynced;
  @BindView(R.id.txt_version) TextView mTxtVersion;
  @BindView(R.id.btn_disconnect) Button mBtnDisconnect;
  @BindView(R.id.txt_new_firmware) TextView mTxtNewFirmware;
  @BindView(R.id.txt_progress) TextView mTxtProgress;
  @BindView(R.id.layout_fota) View mLayoutFota;
  @BindView(R.id.btn_download) Button mBtnDownload;
  @BindView(R.id.btn_install) Button mBtnInstall;
  @BindView(R.id.btn_cancel) Button mBtnCancel;
  private ServiceBinding.Unbind mServiceUnBinder;

  public  int  mDataPower;
  public final String ACTION_MODEL_POWER_RECEIVER="com.mediatek.mt6381eco.biz.home.ACTION_MODEL_POWER_RECEIVER";
  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mServiceUnBinder = ServiceBinding.bindService(this, PeripheralService.class,
        service -> mPresenter.attach((IPeripheral) service));
  }

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    //modify by herman for temp . or info2.
    return inflater.inflate(R.layout.fragment_peripheral_info, container, false);

  }

  @Override protected void applyActionBar(ActionBar actionBar) {
    enableActionBarHome(true);
  }

  @Override protected void initView(Bundle savedInstanceState) {
    mViewModel.deviceName.observe(this, mTxtDeviceName::setText);
    mViewModel.info.observe(this, resource -> {
      switch (resource.status) {
        case LOADING: {
          startLoading(getString(R.string.waiting));
          break;
        }
        case SUCCESS: {
          stopLoading();
          Intent intentP = new  Intent();
          intentP.setAction(ACTION_MODEL_POWER_RECEIVER);
          intentP.putExtra("pData",resource.data.power);
          getActivity().sendBroadcast(intentP);
          Log.d("initView: ","data.power = " + resource.data.power);

          mTxtPower.setText(String.format(Locale.US, "%d%%", resource.data.power));
          mTxtSynced.setText(MTextUtils.formatDateTime(resource.data.synced));

          mTxtVersion.setText(resource.data.version);
          mDataPower = resource.data.power;
          break;
        }
        case ERROR: {
          stopLoading();
          showError(resource.throwable);
          break;
        }
      }
    });
    mViewModel.newFirmware.observe(this, newFirmware -> {
      switch (newFirmware.status) {
        case LOADING:{
          //mTxtNewFirmware.setVisibility(View.VISIBLE);
          mTxtNewFirmware.setText(R.string.checking_new_fw);
          break;
        }
        case SUCCESS: {
          mTxtNewFirmware.setText(newFirmware.data == null ? getString(R.string.up_to_date)
              : getString(R.string.formatter_firmware_available, newFirmware.data));
          //delete by herman
          //mTxtNewFirmware.setVisibility(View.VISIBLE);
          //mLayoutFota.setVisibility(newFirmware.data == null ? View.INVISIBLE : View.VISIBLE);
          //mBtnDownload.setVisibility(newFirmware.data == null ? View.GONE : View.VISIBLE);

          break;
        }
      }
    });

    mViewModel.downloadProgress.observe(this, resource -> {
      switch (resource.status) {
        case LOADING: {
          mBtnDownload.setVisibility(View.GONE);
          mBtnCancel.setVisibility(View.VISIBLE);
          mTxtVersion.setVisibility(View.GONE);
          mTxtNewFirmware.setVisibility(View.GONE);
          mTxtProgress.setVisibility(View.VISIBLE);
          mTxtProgress.setText(getString(R.string.formatter_downloading, resource.data));
          break;
        }
        case ERROR: {
          //delety by herman
          //mBtnDownload.setVisibility(View.VISIBLE);
          mBtnCancel.setVisibility(View.GONE);
          break;
        }
        case SUCCESS: {
          mBtnInstall.setVisibility(View.VISIBLE);
          mBtnDownload.setVisibility(View.GONE);
          mBtnCancel.setVisibility(View.GONE);
          mTxtVersion.setVisibility(View.VISIBLE);
          //mTxtNewFirmware.setVisibility(View.VISIBLE);
          mTxtProgress.setVisibility(View.GONE);
          break;
        }
        case CANCELED: {
          mBtnInstall.setVisibility(View.GONE);
          //delety by herman
          //mBtnDownload.setVisibility(View.VISIBLE);
          mBtnCancel.setVisibility(View.GONE);
          mTxtVersion.setVisibility(View.VISIBLE);
          //mTxtNewFirmware.setVisibility(View.VISIBLE);
          mTxtProgress.setVisibility(View.GONE);
          break;
        }
      }
    });

    mViewModel.fotaProgress.observe(this, resource -> {
      switch (resource.status) {
        case LOADING: {
          enableActionBarHome(false);
          mTxtDeviceName.setEnabled(false);
          mTxtVersion.setVisibility(View.GONE);
          mTxtNewFirmware.setVisibility(View.GONE);
          mTxtProgress.setVisibility(View.VISIBLE);
          mTxtProgress.setText(getString(R.string.formatter_installing, resource.data));
          mBtnInstall.setVisibility(View.GONE);
          mLayoutFota.setVisibility(View.INVISIBLE);
          mBtnDisconnect.setEnabled(false);
          break;
        }
        case SUCCESS: {
          enableActionBarHome(true);
          mBtnDisconnect.setEnabled(true);
          mTxtDeviceName.setEnabled(true);
          mTxtVersion.setVisibility(View.GONE);
          mTxtNewFirmware.setVisibility(View.GONE);
          mBtnInstall.setVisibility(View.GONE);
          mTxtProgress.setVisibility(View.VISIBLE);
          mTxtProgress.setText(getString(R.string.formatter_installing, 100f));
          Toast.makeText(getActivity(), R.string.fota_success, Toast.LENGTH_LONG).show();
          getActivity().finish();

          break;
        }
        case ERROR: {
          enableActionBarHome(true);
          mTxtDeviceName.setEnabled(true);
          mTxtVersion.setVisibility(View.VISIBLE);
          //mTxtNewFirmware.setVisibility(View.VISIBLE);
          mBtnInstall.setVisibility(View.VISIBLE);
          mTxtProgress.setVisibility(View.GONE);
          Toast.makeText(getActivity(), ContextUtils.getErrorMessage(resource.throwable),
              Toast.LENGTH_LONG).show();
          mLayoutFota.setVisibility(View.VISIBLE);
          mBtnDisconnect.setEnabled(true);
          break;
        }
      }
    });
  }

  @OnClick(R.id.btn_download) void onBtnDownloadClick() {
    mPresenter.startDownload();
  }

  @OnClick(R.id.btn_cancel) void onBtnCancelClick() {
    mPresenter.cancelDownload();
  }

  @OnClick(R.id.btn_install) void onBtnInstallClick() {
    new MaterialDialog.Builder(getActivity()).title(R.string.ready_to_install)
        .content(R.string.content_fota_install)
        .positiveText(R.string.install)
        .negativeText(R.string.cancel)
        .onPositive((dialog, which) -> {
          mPresenter.startInstall();
          mTxtDeviceName.setEnabled(false);
        })
        .show();
  }

  @Override public void onDestroy() {
    super.onDestroy();
    mServiceUnBinder.unbind();
    mPresenter.destroy();
  }

  @OnClick(R.id.btn_disconnect) void onBtnDisconnectClick() {
    mPresenter.disconnect();
    getActivity().finish();
  }

  @OnClick(R.id.txt_device_name) void onTxtDeviceNameClick() {
    Log.d("OnClick","onTxtDeviceNameClick!");
    startActivityForResult(
        ContainerActivity.makeIntent(getActivity(), EditPeripheralNameFragment.class)
            .putExtra(EditPeripheralNameFragment.DATA_DEVICE_NAME, mTxtDeviceName.getText()),
        REQUEST_EDIT_NAME);
  }

  @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
      case REQUEST_EDIT_NAME: {
        if (resultCode == Activity.RESULT_OK) {
          mPresenter.changeName(data.getStringExtra(EditPeripheralNameFragment.DATA_DEVICE_NAME));
        }
        break;
      }
    }
  }

  @Override
  public void onBackPressed() {
    Resource<Float> resource = mViewModel.fotaProgress.getValue();
    if (resource == null || resource.status != Status.LOADING) {
      getActivity().finish();
    }
  }

  private void enableActionBarHome(boolean enable) {
    if (getActivity() instanceof AppCompatActivity) {
      AppCompatActivity activity = (AppCompatActivity) getActivity();
      ActionBar actionBar = activity.getSupportActionBar();
      if (actionBar != null) {
        actionBar.setDisplayHomeAsUpEnabled(enable);
        actionBar.setDisplayShowHomeEnabled(enable);
      }
    }
  }

}
