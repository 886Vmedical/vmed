package com.mediatek.mt6381eco.biz.measure.result;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.OnClick;
import com.mediatek.mt6381eco.R;
import com.mediatek.mt6381eco.biz.measure.view.BpCategoryChart;
import com.mediatek.mt6381eco.biz.measure.view.MeasureResultBar;
import com.mediatek.mt6381eco.biz.utlis.BizUtils;
import com.mediatek.mt6381eco.biz.viewmodel.AppViewModel;
import com.mediatek.mt6381eco.ui.BaseActivity;
import javax.inject.Inject;

public class MeasureResultActivity extends BaseActivity implements MeasureResultContract.View {
  public static final int RESULT_CALIBRATION = 101;
  public static final String DATA_NICK_NAME = "DATA_NICK_NAME";
  public static final String DATA_HEART_RATE = "DATA_HEART_RATE";
  public static final String DATA_SPO2 = "SPO2";
  //modify by herman for lowCase
  public static final String DATA_SBP = "sbp";
  public static final String DATA_DBP = "dbp";
  public static final String DATA_FATIGUE = "FATIGUE";
  public static final String DATA_PRESSURE = "PRESSURE";
  public static final String DATA_RISK_LEVEL = "RISK_LEVEL";
  public static final String DATA_CONFIDENCE_LEVEL = "CONFIDENCE_LEVEL";
  public static final String DATA_RISK_PROBABILITY = "RISK_PROBABILITY";
  private final String VMED_NAME = "vemddata";
  @BindView(R.id.txt_heart_rate) TextView mTxtHeartRate;
  @BindView(R.id.txt_bp_systolic) TextView mTxtBpSystolic;
  @BindView(R.id.txt_bp_diastolic) TextView mTxtBpDiastolic;
  @BindView(R.id.txt_spo2) TextView mTxtSpo2;
  @BindView(R.id.txt_brv) TextView mTxtBrv;
  @BindView(R.id.txt_temperature) TextView mTxtTemp;
  @BindView(R.id.txt_fatigue) TextView mTxtFatigue;
  @BindView(R.id.txt_pressure) TextView mTxtPressure;
  @BindView(R.id.txt_heart_rate_risk) TextView mTxtHeartRateRisk;
  @BindView(R.id.txt_measure_result_info) TextView mTxtMeasureResultInfo;
  @BindView(R.id.chart_bp_category) BpCategoryChart mChartBpCategory;
  @BindView(R.id.bar_hr) MeasureResultBar mBarHr;
  @BindView(R.id.bar_spo2) MeasureResultBar mBarSpo2;
  @BindView(R.id.bar_brv) MeasureResultBar mBarBrv;
  @BindView(R.id.bar_temperature) MeasureResultBar mTmeperature;
  @BindView(R.id.bar_fatigue) MeasureResultBar mBarFatigue;
  @BindView(R.id.bar_pressure) MeasureResultBar mBarPressure;
  @BindView(R.id.bar_heart_rate_risk) MeasureResultBar mBarHrRisk;
  @BindView(R.id.view_screening_purchase) LinearLayout mViewScreeningPurchase;
  @BindView(R.id.view_screening) LinearLayout mViewScreening;
  @BindView(R.id.calibrate) LinearLayout calibrate;
  @Inject MeasureResultContract.Presenter mPresenter;
  @Inject AppViewModel mAppViewModel;
  SharedPreferences sPerf;
  @Override protected void onCreate(Bundle savedInstanceState) {
    Log.d("MeasureResultActivity","onCreate");
    super.onCreate(savedInstanceState);
    //setRequestedOrientation(getIntent().getIntExtra(MeasureReadyActivity.ORIENTATION,
    //    Configuration.ORIENTATION_PORTRAIT) == Configuration.ORIENTATION_LANDSCAPE
    //    ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    setContentView(R.layout.activity_measure_result);
    bindViewModel();
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setHomeAsUpIndicator(R.drawable.btn_close);

    setPage();
    setResult(Activity.RESULT_CANCELED);
  }

  private void bindViewModel() {
    mAppViewModel.account.observe(this, account -> {
      boolean hasPermission = account.permission.screening;
      mTxtHeartRateRisk.setVisibility(hasPermission ? View.VISIBLE : View.GONE);
      mBarHrRisk.setVisibility(hasPermission ? View.VISIBLE : View.GONE);
      mViewScreeningPurchase.setVisibility(hasPermission ? View.GONE : View.VISIBLE);
      //modify by herman to gone for all
      calibrate.setVisibility(View.GONE);
      mViewScreening.setVisibility(View.GONE);
    });
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    mPresenter.destroy();
  }

  private String numberToText(int value) {
    if (value < 0) {
      return getString(R.string.empty_value);
    }
    return String.valueOf(value);
  }

  private void setPage() {
    //krestin get spo2 and brv data from measure when people complete  measure start
    sPerf = getSharedPreferences(VMED_NAME, MODE_APPEND);
    SharedPreferences.Editor editData  = sPerf.edit();
    //krestin get spo2 and brv data from measure when people complete  measure end
    Intent intent = getIntent();
    mTxtMeasureResultInfo.setText(
        String.format(getResources().getString(R.string.measure_result_info),
            intent.getStringExtra(DATA_NICK_NAME)));
    //krestin get brv data from measure when people complete  measure start
    int hr = intent.getIntExtra(DATA_HEART_RATE, -1);
    int brv = Integer.parseInt(String.valueOf((int)(hr/4)));
    mTxtHeartRate.setText(numberToText(hr));
    mTxtBrv.setText(numberToText(brv));
    String mBrvStr = Integer.toString(brv);
    mBarHr.setValue(hr);
    mBarBrv.setValue(brv);
    editData.putString("brv",mBrvStr);
    editData.commit();
    //krestin get brv data from measure when people complete  measure end
    int sbp = intent.getIntExtra(DATA_SBP, -1);
    int dbp = intent.getIntExtra(DATA_DBP, -1);
    Log.d("MeasureResultActivity","sbp: " + sbp); //-1
    Log.d("MeasureResultActivity","dbp: " + dbp); //8193
    mTxtBpSystolic.setText(numberToText(sbp));
    mTxtBpDiastolic.setText(numberToText(dbp));
    mChartBpCategory.setBloodPressure(dbp, sbp);

    int spo2 = intent.getIntExtra(DATA_SPO2, -1);
    mTxtSpo2.setText(getString(R.string.percentage_formatter_abbr, spo2));
    mBarSpo2.setValue(spo2);
    //krestin get spo2 data from measure when people complete  measure end
    String mSpo2Str = spo2 + "";
    editData.putString("spo2",mSpo2Str);
    editData.commit();
    //krestin get spo2  data from measure when people complete  measure end
    int fatigue = intent.getIntExtra(DATA_FATIGUE, -1);
    int pressure = intent.getIntExtra(DATA_PRESSURE, -1);
    mTxtFatigue.setText(getString(R.string.percentage_formatter, fatigue));
    mTxtPressure.setText(getString(R.string.percentage_formatter, pressure));
    mBarFatigue.setValue(fatigue);
    mBarPressure.setValue(pressure);

    int riskLevel = intent.getIntExtra(DATA_RISK_LEVEL, 0);
    int riskProbability = intent.getIntExtra(DATA_RISK_PROBABILITY, -1);
    mTxtHeartRateRisk.setText(BizUtils.getHeartRateRiskText(this, riskLevel, riskProbability));
    mBarHrRisk.setValue(riskProbability);

    mChartBpCategory.setVisibility(View.VISIBLE);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home: {
        finish();
        break;
      }
    }
    return true;
  }

  @OnClick({ R.id.txt_calibrate }) public void onClickCalibrate(View view) {
    setResult(RESULT_CALIBRATION);
    finish();
  }

  @OnClick({ R.id.txt_screening_purchase_hint }) public void onClickScreeningPurchase(View view) {
    mPresenter.upgrade()
        .doOnSubscribe(disposable -> startLoading(getString(R.string.loading)))
        .subscribe(this::stopLoading, throwable -> {
          stopLoading();
          showError(throwable);
        });
  }
}
