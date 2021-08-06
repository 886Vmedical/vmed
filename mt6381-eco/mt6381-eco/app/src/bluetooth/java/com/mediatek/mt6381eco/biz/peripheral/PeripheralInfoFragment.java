package com.mediatek.mt6381eco.biz.peripheral;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.OnClick;
import com.mediatek.mt6381eco.R;
import com.mediatek.mt6381eco.biz.peripheral_info.PeripheralInfoContract;
import com.mediatek.mt6381eco.biz.peripheral_info.PeripheralInfoViewModel;
import com.mediatek.mt6381eco.ui.BaseFragment;
import com.mediatek.mt6381eco.utils.MTextUtils;
import java.util.Locale;
import javax.inject.Inject;

public class PeripheralInfoFragment extends BaseFragment {

  @Inject PeripheralInfoContract.Presenter mPresenter;
  @Inject PeripheralInfoViewModel mViewModel;
  @BindView(R.id.txt_power) TextView mTxtPower;
  @BindView(R.id.txt_synced) TextView mTxtSynced;
  @BindView(R.id.txt_version) TextView mTxtVersion;

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_peripheral_info, container, false);
    return view;
  }

  @Override protected void initView(Bundle savedInstanceState) {

    mViewModel.info.observe(this, resource -> {
      switch (resource.status) {
        case LOADING: {
          startLoading(getString(R.string.waiting));
          break;
        }
        case SUCCESS: {
          stopLoading();
          mTxtPower.setText(String.format(Locale.US, "%d%%", resource.data.power));
          mTxtSynced.setText(MTextUtils.formatDateTime(resource.data.synced));
          mTxtVersion.setText(resource.data.version);
          break;
        }
        case ERROR: {
          stopLoading();
          showError(resource.throwable);
          break;
        }
      }
    });
  }

  public void attach(IPeripheral peripheral) {
    mPresenter.attach(peripheral);
  }

  @OnClick(R.id.btn_disconnect) void onBtnDisconnect() {
    mPresenter.disconnect();
    getActivity().finish();
  }
}
