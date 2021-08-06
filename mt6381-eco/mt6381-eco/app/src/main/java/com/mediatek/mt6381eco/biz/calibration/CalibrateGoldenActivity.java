package com.mediatek.mt6381eco.biz.calibration;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mediatek.mt6381eco.R;
import com.mediatek.mt6381eco.rxbus.RxBus;
import com.mediatek.mt6381eco.validation.CompositeValidation;
import com.mediatek.mt6381eco.validation.NumberValidate;
import com.mediatek.mt6381eco.validation.RequiredValidate;
import com.mediatek.mt6381eco.validation.ValidateEvent;
import com.mediatek.mt6381eco.validation.ViewValidation;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import io.reactivex.disposables.CompositeDisposable;

public class CalibrateGoldenActivity extends AppCompatActivity {

  public static final String STEP = "step";
  public static final String DATA_SBP = "sbp";
  public static final String DATA_DBP = "dbp";
  public static final String DATA_HR = "heartRate";

  @BindView(R.id.edt_sbp) EditText mEdtSbp;
  @BindView(R.id.edt_dbp) EditText mEdtDbp;
  @NotEmpty @BindView(R.id.txt_heart_rate) EditText mEdtHeartRate;
  @BindView(R.id.btn_next) Button mBtnNext;

  private final CompositeValidation mValidations = new CompositeValidation();
  private final CompositeDisposable mDisposables = new CompositeDisposable();

  @Override protected void onCreate(Bundle savedInstanceState) {
    Log.d("CalibrateGoldenActivity","onCreate()");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_calibrate_golden);

    ButterKnife.bind(this);
    setResult(Activity.RESULT_CANCELED);
    int step = getIntent().getIntExtra(STEP, 0);
    int titleResId = R.string.input_golden;
    if (step > 0 ) {
      titleResId = R.string.input_golden_again;
    }
    getSupportActionBar().setTitle(titleResId);
    initFormValid();
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    mDisposables.clear();
  }

  private void initFormValid() {
    mValidations.clear();
    RequiredValidate requiredValidate = new RequiredValidate(this.getApplication());
    mValidations.addValidation(new ViewValidation(mEdtSbp).addValid(requiredValidate)
        .addValid(new NumberValidate(this.getApplication(), 80, 190)));
    mValidations.addValidation(new ViewValidation(mEdtDbp).addValid(requiredValidate)
        .addValid(new NumberValidate(this.getApplication(), 50, 120)));
    mValidations.addValidation(new ViewValidation(mEdtHeartRate).addValid(requiredValidate));

    mDisposables.add(RxBus.getInstance()
        .toFlowable(ValidateEvent.class)
        .subscribe(validateEvent -> mBtnNext.setEnabled(
            validateEvent.isValid() && mValidations.isValid())));
  }

  @OnClick(R.id.btn_next) void onBtnOnNextClick() {
    Intent intent = getIntent();
    int sbp = Integer.valueOf(mEdtSbp.getText().toString());
    int dbp = Integer.valueOf(mEdtDbp.getText().toString());
    int heartRate = Integer.valueOf(mEdtHeartRate.getText().toString());

    intent.putExtra(DATA_SBP, sbp);
    intent.putExtra(DATA_DBP, dbp);
    intent.putExtra(DATA_HR, heartRate);
    setResult(Activity.RESULT_OK, intent);
    finish();
  }

  @OnClick(R.id.btn_cancel) void onBtnCancelClick() {
    new MaterialDialog.Builder(this).content(R.string.cancel_calibrate)
        .positiveText(R.string.yes)
        .negativeText(R.string.no)
        .cancelable(false)
        .onPositive((dialog, which) -> {
          setResult(Activity.RESULT_CANCELED);
          finish();
        })
        .show();
  }

  @Override public void onBackPressed() {
    super.onBackPressed();
    onBtnCancelClick();
  }
}
