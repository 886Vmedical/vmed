package com.mediatek.mt6381eco.biz.measure;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnLongClick;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mediatek.mt6381eco.R;
import com.mediatek.mt6381eco.biz.calibration.CalibrationActivity;
import com.mediatek.mt6381eco.biz.measure.ready.MeasureReadyActivity;
import com.mediatek.mt6381eco.biz.measure.result.MeasureResultActivity;
import com.mediatek.mt6381eco.biz.measure.view.CountdownPb;
import com.mediatek.mt6381eco.biz.measure.view.RealTimeWaveformView;
import com.mediatek.mt6381eco.biz.peripheral.IPeripheral;
import com.mediatek.mt6381eco.biz.peripheral.PeripheralService;
import com.mediatek.mt6381eco.biz.peripheral.SensorData;
import com.mediatek.mt6381eco.biz.utlis.BizUtils;
import com.mediatek.mt6381eco.dagger.Injectable;
import com.mediatek.mt6381eco.dagger.SupportSensorTypes;
import com.mediatek.mt6381eco.ui.BaseFragment;
import com.mediatek.mt6381eco.ui.ContextUtils;
import com.mediatek.mt6381eco.utils.DataUtils;
import com.mediatek.mt6381eco.utils.ServiceBinding;
import com.mediatek.mt6381eco.viewmodel.Status;
import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

public class MeasureFragment extends BaseFragment implements Injectable {
  private static final int REQUEST_READY = 1;
  private static final int REQUEST_MEASURE_RESULT = 2;
  @Inject BaseMeasureViewModel mViewModel;
  @Inject MeasureViewModel mCompletdViewModel;
  @Inject MeasureContract.Presenter mPresenter;
  @Inject SupportSensorTypes mSupportSensorTypes;

  @BindView(R.id.txt_name) TextView mTxtNickName;
  @BindView(R.id.wave_form)
  RealTimeWaveformView mWaveForm;
  @BindView(R.id.txt_cd_count) TextView mTxtCdCount;
  @BindView(R.id.view_countdown) CountdownPb mCdProgress;
  @BindView(R.id.txt_heart_rate) TextView mTxtHeartRate;
  @BindView(R.id.txt_spo2) TextView mTxtSpo2;
  @BindView(R.id.txt_blood_pressure) TextView mTxtBloodPressure;
  @BindView(R.id.view_countdown_fatigue) CountdownPb mCdFatigue;
  @BindView(R.id.view_countdown_pressure) CountdownPb mCdPressure;
  @BindView(R.id.txt_fatigue) TextView mTxtFatigue;
  @BindView(R.id.txt_pressure) TextView mTxtPressure;
  @BindView(R.id.txt_measure_status) TextView mTxtState;
  @BindView(R.id.view_finger_on_off) RelativeLayout mLayoutFingerOnOff;
  @BindView(R.id.txt_finger_msg) TextView mTxtCheckReason;
  @BindView(R.id.btn_finish) Button mBtnFinish;
  @BindView(R.id.btn_cancel) Button mBtnCancel;
  @BindView(R.id.btn_start) Button mBtnStart;
  @BindView(R.id.btn_play) ImageButton mBtnPlay;
  @BindView(R.id.btn_pause) ImageButton mBtnPause;
  @BindView(R.id.txt_remeasure) TextView mTxtRemeasure;
  @BindView(R.id.txt_ekg) TextView mTxtEkg;
  @BindView(R.id.txt_ppg) TextView mTxtPpg;
  @BindView(R.id.txt_ppg2) TextView mTxtPpg2;
  private ServiceBinding.Unbind mServiceUnbinder;
  private IPeripheral mIperipheral;
  protected CompositeDisposable mDisposables = new CompositeDisposable();

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    Log.d("MeasureFragment","onCreate（）");
    super.onCreate(savedInstanceState);
    mServiceUnbinder = ServiceBinding.bindService(this, PeripheralService.class, service -> {
      mIperipheral = (IPeripheral) service;
      mPresenter.attach(mIperipheral);
    });

    if (savedInstanceState != null) {
      restore(savedInstanceState);
    } else {
      alertMeasureReady();
    }
  }

  private void restore(Bundle savedInstanceState) {
    MeasureContract.PresenterState saveState = new MeasureContract.PresenterState();
    saveState.stateName =
        savedInstanceState.getString(MeasureContract.PresenterState.KEY_STATE_NAME);
    saveState.transObject =
        savedInstanceState.getSerializable(MeasureContract.PresenterState.KEY_TRANS_OBJECT);
    assert saveState.transObject != null;
    mPresenter.restoreSaveState(saveState);
  }

  @Override public void onDestroy() {
    super.onDestroy();
    mServiceUnbinder.unbind();
    mPresenter.destroy();
  }

  @Override protected void initView(Bundle savedInstanceState) {
    int[] sensorTypes = mSupportSensorTypes.getTypeIntArray();
    mWaveForm.initTypes(sensorTypes);
    mTxtEkg.setVisibility(
        DataUtils.indexOf(sensorTypes, SensorData.DATA_TYPE_EKG) > -1 ? View.VISIBLE : View.GONE);
    mTxtPpg.setVisibility(
        DataUtils.indexOf(sensorTypes, SensorData.DATA_TYPE_PPG1) > -1 ? View.VISIBLE : View.GONE);


    //modify by herman for ppg2 GONE
    /*mTxtPpg2.setVisibility(
        DataUtils.indexOf(sensorTypes, SensorData.DATA_TYPE_PPG2) > -1 ? View.VISIBLE : View.GONE);*/
    mTxtPpg2.setVisibility(View.GONE);


    mViewModel.dataLostType.observe(this, dataLostType -> {
      switch (dataLostType) {
        case DataLostEvent.DATA_TYPE_EKG: {
          mTxtEkg.setText(getResources().getString(R.string.ecg_title) + ".");
          break;
        }
        default: {
          mTxtPpg.setText(getResources().getString(R.string.ppg_title) + ".");
        }
      }
    });

    mViewModel.nickname.observe(this, mTxtNickName::setText);
    for (int i = 0; i < mViewModel.waveData.size(); ++i) {
      int key = mViewModel.waveData.keyAt(i);
      mWaveForm.setData(key, mViewModel.waveData.get(key));
    }
    mViewModel.progress.observe(this, progress -> {
      mTxtCdCount.setText(String.valueOf(progress));
      mCdProgress.setProgress(progress);
    });

    mViewModel.hrspo2.observe(this, hrSpo2 -> {
      mTxtHeartRate.setText(numberToText(hrSpo2.heartRate));
      mTxtSpo2.setText(numberToText(hrSpo2.spo2));
      Log.d("initView()","hrSpo2: " + hrSpo2);
      Log.d("initView()","hrSpo2.heartRate: " + hrSpo2.heartRate);
      Log.d("initView()","hrSpo2.spo2: " + hrSpo2.spo2);
    });

    mViewModel.bloodPressure.observe(this, bloodPressure -> mTxtBloodPressure.setText(
        String.format("%s/%s", numberToText(bloodPressure.sbp), numberToText(bloodPressure.dbp))));


    mViewModel.fatiguePressure.observe(this, fatiguePressure -> {
      mTxtFatigue.setText(numberToText(fatiguePressure.fatigue));
      mCdFatigue.setProgress(fatiguePressure.fatigue);
      mTxtPressure.setText(numberToText(fatiguePressure.pressure));
      mCdPressure.setProgress(fatiguePressure.pressure);
    });

    mViewModel.state.observe(this, state -> {
      final int[] txtIds = new int[] {
          R.string.measure_status_measuring, R.string.measure_status_measuring,
          R.string.measure_status_checking, R.string.measure_status_interrupted,
          R.string.measure_status_completed
      };
      if (state < txtIds.length) {
        mTxtState.setText(txtIds[state]);
      }
      mBtnCancel.setVisibility(state == BaseMeasureViewModel.STATE_MEASURING
          || state == BaseMeasureViewModel.STATE_CHECKING ? View.VISIBLE : View.GONE);
      mBtnFinish.setVisibility(
          state == BaseMeasureViewModel.STATE_COMPLETED ? View.VISIBLE : View.GONE);
      mBtnStart.setVisibility(
          state == BaseMeasureViewModel.STATE_COMPLETED ? View.VISIBLE : View.GONE);
    });
    mViewModel.toCheckReason.observe(this, reason -> {
      mLayoutFingerOnOff.setVisibility(
          reason > 0 ? View.VISIBLE : View.GONE);
      if (reason > 0) {
        mTxtCheckReason.setText(getString(R.string.bad_signal_tips, getString(BizUtils.getBadSignalStringIdRes(reason))));
      }
    });
    mViewModel.interruptError.observe(this, throwable -> {
      if (throwable != null) {
        showInterrupt(throwable);
      }
    });

    mCompletdViewModel.result.observe(this, resource -> {
      if (resource != null) {
        switch (resource.status) {
          case LOADING: {
            startLoading(getString(R.string.uploading));
            break;
          }
          case SUCCESS: {
            stopLoading();
            //Herman 展示测量数据  and 发送命令
            Log.d("MeasureFragment","sendCommandforfinish start...");
            sendCommandforfinish();
            showMeasurementResult(resource.data);
            Log.d("MeasureFragment","showMeasurementResult end...");
            mCompletdViewModel.result.setValue(null);
            break;
          }
          case ERROR: {
            Toast.makeText(getActivity(), ContextUtils.getErrorMessage(resource.throwable),
                Toast.LENGTH_LONG).show();
            stopLoading();
            mCompletdViewModel.result.setValue(null);
            break;
          }
        }
      }
    });
    mViewModel.mPrepareLoading.observe(this, resource -> {
      switch (resource.status) {
        case LOADING: {
          startLoading("");
          break;
        }
        default: {
          stopLoading();
        }
      }
    });
    mViewModel.replayState.observe(this, state -> {
      mBtnPause.setVisibility(View.GONE);
      mBtnPlay.setVisibility(state == BaseMeasureViewModel.REPLAY_PAUSE ? View.VISIBLE : View.GONE);
    });

    mViewModel.remeasure.observe(this, resource -> mTxtRemeasure.setVisibility(
        resource != null && resource.status == Status.LOADING ? View.VISIBLE : View.GONE));

    mViewModel.ekgChecking.observe(this, isChecking -> mTxtEkg.setAlpha(isChecking ? 1f : 0.4f));
    mViewModel.ppg1Checking.observe(this, isChecking -> mTxtPpg.setAlpha(isChecking ? 1f : 0.4f));
    mViewModel.ppg2Checking.observe(this, isChecking -> mTxtPpg2.setAlpha(isChecking ? 1f : 0.4f));
  }

  private void showMeasurementResult(MeasureViewModel.Result result) {
    startActivityForResult(new Intent(getActivity(), MeasureResultActivity.class).putExtra(
        MeasureReadyActivity.ORIENTATION, getResources().getConfiguration().orientation)
            .putExtra(MeasureResultActivity.DATA_NICK_NAME, mViewModel.nickname.getValue())
            .putExtra(MeasureResultActivity.DATA_HEART_RATE, result.heartRate)
            .putExtra(MeasureResultActivity.DATA_SPO2, result.spo2)
            .putExtra(MeasureResultActivity.DATA_DBP, result.dbp)
            .putExtra(MeasureResultActivity.DATA_SBP, result.sbp)
            .putExtra(MeasureResultActivity.DATA_FATIGUE, result.fatigue)
            .putExtra(MeasureResultActivity.DATA_PRESSURE, result.pressure)
            .putExtra(MeasureResultActivity.DATA_RISK_LEVEL, result.riskLevel)
            .putExtra(MeasureResultActivity.DATA_CONFIDENCE_LEVEL, result.confidenceLevel)
            .putExtra(MeasureResultActivity.DATA_RISK_PROBABILITY, result.riskProbability),
        REQUEST_MEASURE_RESULT);
    Log.d("showMeasurementResult","result.dbp: " + result.dbp);//8197
    Log.d("showMeasurementResult","result.sbp: " + result.sbp);//-1
  }

  private void showInterrupt(Throwable throwable) {
    if (throwable != null && throwable instanceof InterruptException) {
      MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity()).cancelable(false);
      switch (((InterruptException) throwable).type) {
        case InterruptException.TYPE_CONNECTION_LOST: {
          builder.title(R.string.bp_measure_exception_title)
              .content(R.string.connection_lost)
              .positiveText(R.string.exit)
              .dismissListener(dialog -> getActivity().finish());
          break;
        }
        case InterruptException.TYPE_FINER_OFF: {
          builder.title(R.string.bp_measure_exception_title)
              .content(R.string.measure_interrupt_msg)
              .negativeText(R.string.no)
              .onNegative((dialog, which) -> getActivity().finish())
              .positiveText(R.string.measure_interrupt_yes)
              .onPositive((dialog, which) -> alertMeasureReady());
          break;
        }
        case InterruptException.TYPE_START_MEASURE_FAIL: {
          builder.title(R.string.bp_measure_exception_title)
              .content(R.string.error_start_measure_fail)
              .positiveText(R.string.exit)
              .dismissListener(dialog -> getActivity().finish());
          break;
        }
        case InterruptException.TYPE_ERROR: {
          builder.title(R.string.bp_measure_exception_title)
              .content(ContextUtils.getErrorMessage(((InterruptException) throwable).error))
              .positiveText(R.string.exit)
              .dismissListener(dialog -> getActivity().finish());
          break;
        }
        case InterruptException.TYPE_STATE_LOST: {
          builder.title(R.string.bp_measure_exception_title)
              .content(R.string.error_measurement_interrupted)
              .positiveText(R.string.exit)
              .dismissListener(dialog -> getActivity().finish());
          break;
        }
        case InterruptException.TYPE_ABORT: {
          builder.title(R.string.bp_measure_exception_title)
              .content(R.string.error_measurement_aborted)
              .positiveText(R.string.exit)
              .dismissListener(dialog -> getActivity().finish());
          break;
        }
      }
      builder.show();
    }
  }

  private String numberToText(int value) {
    if (value < 0) {
      return getString(R.string.empty_value);
    }
    return String.valueOf(value);
  }

  private void alertMeasureReady() {
    startActivityForResult(
        new Intent(getActivity(), MeasureReadyActivity.class).putExtra(MeasureReadyActivity.FROM,
            MeasureReadyActivity.FROM_MEASURE), REQUEST_READY);
  }

  @OnClick(R.id.btn_finish) void onFinishClick() {
    getActivity().finish();
  }

  @OnClick(R.id.btn_cancel) void onBtnCancelClick() {
    getActivity().finish();
  }

  @OnClick(R.id.btn_start) void onBtnStartClick() {
    alertMeasureReady();
  }

  @OnClick(R.id.btn_play) void onBtnPlayClick() {
    mPresenter.startReplay();
  }

  @OnClick(R.id.btn_pause) void onBtnPauseClick() {
    mPresenter.pauseReplay();
  }

  @OnClick(R.id.wave_form) void onWaveFormClick() {
    if (mViewModel.replayState.getValue() == BaseMeasureViewModel.REPLAY_PLAY) {
      mBtnPause.setVisibility(mBtnPause.isShown() ? View.GONE : View.VISIBLE);
    }
  }

  @OnLongClick(R.id.txt_ekg) boolean onTxtEkgClick(){
    mPresenter.toggleEKGChecking();
    return true;
  }

  @OnLongClick(R.id.txt_ppg) boolean onTxtPPG1Click(){
    mPresenter.togglePPG1Checking();
    return true;
  }

  @OnLongClick(R.id.txt_ppg2) boolean onTxtPPG2Click(){
    mPresenter.togglePPG2Checking();
    return true;
  }



  @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
    Log.d("MeasureFragment","requestCode: " + requestCode);//2
    Log.d("MeasureFragment","resultCode: " + resultCode);//0
    switch (requestCode) {
      case REQUEST_READY: {
        if (Activity.RESULT_OK == resultCode) {
          if (mServiceUnbinder.isBonded()) {
            mPresenter.startMeasure(
                data != null && data.getBooleanExtra(MeasureReadyActivity.DATA_DOWN_SAMPLE, false));
          } else {
            Timber.w("Service can not be bond");
          }
        } else {
          getActivity().finish();
        }
        break;
      }
      case REQUEST_MEASURE_RESULT: {
        if (resultCode == MeasureResultActivity.RESULT_CALIBRATION) {
          //todo by herman for Calibration
          startActivity(new Intent(getActivity(), CalibrationActivity.class));
          getActivity().finish();
        }
        break;
      }
    }
  }

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_measure2, container, false);
  }

  @Override public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    MeasureContract.PresenterState saveState = mPresenter.getSaveState();
    outState.putString(MeasureContract.PresenterState.KEY_STATE_NAME, saveState.stateName);
    outState.putSerializable(MeasureContract.PresenterState.KEY_TRANS_OBJECT,
        saveState.transObject);
  }

  @Override public void onStop() {
    super.onStop();
    if(!getActivity().isChangingConfigurations()){
      mPresenter.abort();
    }
  }

  public void sendCommandforfinish(){
    Log.d("MeasureFragment","sendCommandforfinish...");
    mDisposables.add(mIperipheral.sendMeasureFinish()
            .subscribe(() -> { Timber.d("sendCommandforfinish: success"); },
                    throwable -> {
                      Timber.d("sendCommandforfinish: success but has exception,no matter");
                      Timber.w(throwable); }
            ));
  }
}
