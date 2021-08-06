package com.mediatek.mt6381eco.biz.recorddetail;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.BindView;
import com.gturedi.views.StatefulLayout;
import com.mediatek.mt6381eco.R;
import com.mediatek.mt6381eco.biz.utlis.BizUtils;
import com.mediatek.mt6381eco.dagger.Injectable;
import com.mediatek.mt6381eco.ui.BaseFragment;
import com.mediatek.mt6381eco.ui.ContextUtils;
import com.mediatek.mt6381eco.ui.widgets.WaveformView;
import com.mediatek.mt6381eco.viewmodel.Status;
import java.util.ArrayList;
import java.util.Locale;

import javax.inject.Inject;
import timber.log.Timber;

public class RecordDetailFragment extends BaseFragment implements Injectable {
  @Inject RecordDetailViewModel mViewModel;
  @Inject RecordDetailContract.Presenter mPresenter;
  @BindView(R.id.chart)
  WaveformView mWaveformView;
  @BindView(R.id.txt_duration) TextView mTxtDuration;
  @BindView(R.id.layout_stateful) StatefulLayout mLayoutStateful;
  @BindView(R.id.txt_blood_pressure) TextView mTxtBloodPressure;
  @BindView(R.id.txt_heart_rate) TextView mTxtHeartRate;
  @BindView(R.id.txt_spo2) TextView mTxtSpo2;
  @BindView(R.id.txt_brv) TextView mTxtBrv;
  @BindView(R.id.txt_temperature) TextView mTxtTemperature;
  @BindView(R.id.txt_fatigue_index) TextView mTxtFatigue;
  @BindView(R.id.txt_pressure_index) TextView mTxtPressure;
  @BindView(R.id.txt_heart_rate_risk) TextView mTxtHeartRateRisk;
  @BindView(R.id.txt_ecg) TextView mTxtEcg;
  @BindView(R.id.txt_ppg) TextView mTxtPpg;
  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_record_detail, container, false);
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    String profileId = getActivity().getIntent().getStringExtra(RecordDetailActivity.PROFILE_ID);
    int measurementId =
        getActivity().getIntent().getIntExtra(RecordDetailActivity.MEASUREMENT_ID, -1);
    mPresenter.loadMetaData(profileId, measurementId);
    mPresenter.loadWaveformData(profileId, measurementId);
  }

  @Override protected void initView(Bundle savedInstanceState) {
    mWaveformView.setOnScrollListener(
        (start, end) -> mTxtDuration.setText(getString(R.string.wave_form_duration, start, end)));
    mViewModel.meta.observe(this, resource -> {
      switch (resource.status) {
        case LOADING: {
          mLayoutStateful.showLoading();
          break;
        }
        case ERROR: {
          Timber.e(resource.throwable);
          mLayoutStateful.showError(ContextUtils.getErrorMessage(resource.throwable), null);
          break;
        }
        case SUCCESS: {
          showContentIfNeed();
          mTxtBloodPressure.setText(
              getString(R.string.blood_pressure_formatter, resource.data.systolic,
                  resource.data.diastolic));

          mTxtHeartRate.setText(String.valueOf(resource.data.heartRate));
          mTxtSpo2.setText(String.valueOf(resource.data.spo2));
          //krestin add to display brv data in result record start
          mTxtBrv.setText(String.valueOf(resource.data.heartRate/4));
          //krestin add to display °C when in chinese language
          if(!isChinese()) {
            if(resource.data.temperature == 0.0)
            {
              mTxtTemperature.setText(R.string.default_no_measure);
            }else {
              mTxtTemperature.setText(String.valueOf(resource.data.temperature) + " °F");
            }
          }else{
             if(resource.data.temperature == 0.0) {
               mTxtTemperature.setText(R.string.default_no_measure);
             }else {
               double tempChinese = ((resource.data.temperature) - 32) / 1.8;
               String tempCH = String.format("%.2f", tempChinese);
               mTxtTemperature.setText(tempCH + " °C");
             }
          }
          //krestin add to display brv data in result record end
          mTxtFatigue.setText(getString(R.string.percentage_formatter, resource.data.fatigue));
          mTxtPressure.setText(getString(R.string.percentage_formatter, resource.data.pressure));
          mTxtHeartRateRisk.setText(
              BizUtils.getHeartRateRiskText(getContext(), resource.data.riskLevel,
                  resource.data.riskProbability));
        }
      }
    });
    mViewModel.rawData.observe(this, resource -> {
      switch (resource.status) {
        case LOADING: {
          mLayoutStateful.showLoading();
          break;
        }
        case ERROR: {
          Timber.e(resource.throwable);
          mLayoutStateful.showError(ContextUtils.getErrorMessage(resource.throwable), null);
          break;
        }
        case SUCCESS: {
          showContentIfNeed();
          ArrayList<Float> eck = resource.data.first;
          ArrayList<Float> ppg = resource.data.second;
          if (!eck.isEmpty()) {
            mWaveformView.addData(eck, ContextCompat.getColor(getContext(), R.color.ecg_color));
          }
          if (!ppg.isEmpty()) {
            mWaveformView.addData(ppg, ContextCompat.getColor(getContext(), R.color.ppg_color));
          }
          mTxtEcg.setVisibility(eck.isEmpty()?View.GONE:View.VISIBLE);
          mTxtPpg.setVisibility(ppg.isEmpty()?View.GONE:View.VISIBLE);
        }
      }
    });
  }

  public boolean isChinese() {
    Locale locale = Locale.getDefault();
    String language = locale.getLanguage();
    return "zh".equals(language);
  }

  private void showContentIfNeed() {
    if (mViewModel.meta.getValue() != null
        && mViewModel.meta.getValue().status == Status.SUCCESS
        && mViewModel.rawData.getValue() != null
        && mViewModel.rawData.getValue().status == Status.SUCCESS) {
      mLayoutStateful.showContent();
    }
  }
}
