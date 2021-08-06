package com.mediatek.mt6381eco.biz.peripheral_info;

import android.app.Activity;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mediatek.mt6381eco.R;
import com.mediatek.mt6381eco.biz.peripheral.IBlePeripheral;
import com.mediatek.mt6381eco.biz.peripheral.PeripheralService;
import com.mediatek.mt6381eco.dagger.Injectable;
import com.mediatek.mt6381eco.ui.BaseFragment;
import com.mediatek.mt6381eco.ui.ContextUtils;
import com.mediatek.mt6381eco.ui.OnBackPressedListener;
import com.mediatek.mt6381eco.utils.ServiceBinding;
import com.mediatek.mt6381eco.viewmodel.Resource;
import com.mediatek.mt6381eco.viewmodel.Status;

import io.reactivex.disposables.CompositeDisposable;

public class EditPeripheralNameFragment extends BaseFragment  {
  public static final String DATA_DEVICE_NAME = "DEVICE_NAME";
  private static final int MIN_DEVICE_LENGTH = 6;
  private final MutableLiveData<Resource> mActionChangeName = new MutableLiveData<>();
  @BindView(R.id.edt_device_name) EditText mEdtDeviceName;
  @BindView(R.id.btn_save) Button mBtnSave;
  private ServiceBinding.Unbind mServiceUnBinder;
  private final CompositeDisposable mDisposables = new CompositeDisposable();
  private IBlePeripheral mPeripheral;

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d("EditFragment","onCreate");
    setHasOptionsMenu(true);
    mServiceUnBinder = ServiceBinding.bindService(this, PeripheralService.class,
        service -> mPeripheral = (IBlePeripheral) service);
  }

  @Override protected void applyActionBar(ActionBar actionBar) {
    super.applyActionBar(actionBar);
    setHasOptionsMenu(true);
    Log.d("EditFragment","applyActionBar");
    actionBar.setTitle(R.string.edit_device_name);
  }

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    Log.d("EditFragment","onCreateView");
    return inflater.inflate(R.layout.fragment_edit_peripheral_name, container, false);
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    Log.d("EditFragment","onViewCreated");
    if (savedInstanceState == null) {
      mEdtDeviceName.setOnKeyListener(new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
          if (keyCode == KeyEvent.KEYCODE_BACK
                  && event.getAction() == KeyEvent.ACTION_UP) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mEdtDeviceName.getWindowToken(), 0);
            editAttention();
            return true;
          }
          return false;
        }
      });
      mEdtDeviceName.setText(getArguments().getString(DATA_DEVICE_NAME));
      mEdtDeviceName.selectAll();
    }
  }

  @Override public void onDestroy() {
    super.onDestroy();
    mServiceUnBinder.unbind();
    mDisposables.clear();
  }

  @Override protected void initView(Bundle savedInstanceState) {
    Log.d("EditFragment","initView");
    mActionChangeName.observe(this, resource -> {
      switch (resource.status) {
        case LOADING: {
          startLoading(getString(R.string.waiting));
          break;
        }
        case ERROR: {
          Toast.makeText(getContext(), ContextUtils.getErrorMessage(resource.throwable),
              Toast.LENGTH_LONG).show();
          stopLoading();
          break;
        }
        case SUCCESS: {
          getActivity().setResult(Activity.RESULT_OK,
              new Intent().putExtra(DATA_DEVICE_NAME, mEdtDeviceName.getText().toString()));
          getActivity().finish();
          break;
        }
      }
    });
  }

  @OnTextChanged(R.id.edt_device_name) void onEdtDeviceNameTextChange(CharSequence text) {
    mBtnSave.setEnabled(text.length() >=6 /*&&
        !text.toString().equals(getArguments().getString(DATA_DEVICE_NAME))*/);

  }

  @OnClick(R.id.btn_save) void onBtnSaveClick() {
    mDisposables.add(mPeripheral.setDeviceName(mEdtDeviceName.getText().toString())
        .doOnSubscribe(disposable -> mActionChangeName.postValue(Resource.loading(null)))
        .subscribe(() -> mActionChangeName.postValue(Resource.success(null)),
            throwable -> mActionChangeName.postValue(Resource.error(throwable, null))));
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home: {
        Log.d("editAttention","onOptionsItemSelected!");
        Toast.makeText(getActivity(),"AS UP Doing！",Toast.LENGTH_LONG).show();
        break;
      }
    }
    return super.onOptionsItemSelected(item);
  }

  public void editAttention(){
    MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
    Log.d("editAttention","DeviceName =" + mEdtDeviceName.getText().toString());
    String deviceName = mEdtDeviceName.getText().toString();
    if(mEdtDeviceName.length() < 6){
      builder.title(R.string.attention)
              .content(R.string.device_null)
              .positiveText(R.string.ok)
              .onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                  mEdtDeviceName.setText(getArguments().getString(DATA_DEVICE_NAME));
                }
              });
    }else{
      builder.title(R.string.attention)
              .content(R.string.device_name_save)
              .negativeText(R.string.no)
              .onNegative(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                }
              })
              .positiveText(R.string.measure_interrupt_yes)
              .onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                  getActivity().finish();
                }
              });
    }
    builder.show();
  }

  @Override
  public void onResume() {
    super.onResume();
    getView().setFocusableInTouchMode(true);
    getView().requestFocus();
    getView().setOnKeyListener(new View.OnKeyListener() {
      @Override
      public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
          editAttention();
          return true;
        }
        return false;
      }
    });
  }

}
