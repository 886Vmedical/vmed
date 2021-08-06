package com.mediatek.mt6381eco.biz.measure.ready;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;
import butterknife.OnClick;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mediatek.mt6381eco.R;
import com.mediatek.mt6381eco.biz.peripheral.IPeripheral;
import com.mediatek.mt6381eco.exceptions.ConnectionLostException;
import com.mediatek.mt6381eco.exceptions.ThroughputException;
import com.mediatek.mt6381eco.ui.BasePeripheralActivity;
import javax.inject.Inject;
import timber.log.Timber;

public class MeasureReadyActivity extends BasePeripheralActivity
    implements MeasureReadyContract.View {
  public static final String FROM ="FROM";
  public static final int FROM_MEASURE = 1;
  public static final int FROM_CALIBRATION = 2;
  public static final String ORIENTATION = "useless";
  public static final String DATA_DOWN_SAMPLE = "DOWN_SAMPLE";
  @Inject MeasureReadyContract.Presenter mPresenter;

  @Override protected void onCreate(Bundle savedInstanceState) {
    Log.d("MeasureReadyActivity","onCreate（）");
    super.onCreate(savedInstanceState);
    setContentView();
    mPresenter.setView(this);
    setResult(Activity.RESULT_CANCELED);
    changeTitle( getIntent().getIntExtra(FROM, FROM_MEASURE));
    //changeTitle( getIntent().getIntExtra(FROM, FROM_CALIBRATION));

  }

  private void changeTitle(int from) {
    int titleResId = R.string.measure_ready_title;
    if(FROM_CALIBRATION == from){
      titleResId = R.string.ready_to_calibration;
    }
    getSupportActionBar().setTitle(titleResId);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    mPresenter.destroy();
  }

  @Override protected void attach(IPeripheral peripheral) {
    super.attach(peripheral);
    mPresenter.attach(peripheral);
  }

  @Override public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    setContentView();
  }

  @OnClick(R.id.btn_cancel) public void onBtnCancelClick() {
    mPresenter.stopMeasure();
    navHomePage();
  }

  @Override public void onBackPressed() {
    onBtnCancelClick();
  }

  @Override public void navMeasurePage(boolean downSample) {
    Intent data = new Intent();
    if(downSample){
      Toast.makeText(this, R.string.down_sample, Toast.LENGTH_SHORT).show();
    }
    data.putExtra(DATA_DOWN_SAMPLE, downSample);
    setResult(Activity.RESULT_OK, data);
    finish();
  }


  @Override public void navHomePage() {
    finish();
  }

  @Override public void setContentView() {
    setContentView(R.layout.activity_measure_ready);
  }

  @Override public void alterThroughput(long throughput, int allowMinThroughput) {
    uiAction(() -> new MaterialDialog.Builder(this).title(R.string.slow_throughput)
        .content(R.string.throughput_warning_formatter, throughput / 1000f)
        .positiveText(R.string.exit)
        .onPositive((dialog, which) -> finish())
        .show());
  }

  @Override public void exit() {
    uiAction(this::finish);
  }

  @Override public void showError(Throwable throwable) {
    Timber.e(throwable, throwable.getMessage());
    uiAction(() -> {
      MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
          .title(R.string.meet_an_error)
          .positiveText(R.string.exit)
          .dismissListener(dialogInterface -> finish());
      if (throwable instanceof ConnectionLostException) {
        builder.content(R.string.connection_lost);
      } else if (throwable instanceof ThroughputException) {
        builder.content(R.string.test_throughput_error);
      } else {
        builder.content(throwable.getMessage());
      }
      builder.show();
    });
  }
}