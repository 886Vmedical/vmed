package com.mediatek.mt6381eco.biz.calibration;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnLongClick;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mediatek.mt6381eco.R;
import com.mediatek.mt6381eco.biz.measure.BaseMeasureViewModel;
import com.mediatek.mt6381eco.biz.measure.InterruptException;
import com.mediatek.mt6381eco.biz.measure.MeasureActivity;
import com.mediatek.mt6381eco.biz.measure.MeasureContract;
import com.mediatek.mt6381eco.biz.measure.ready.MeasureReadyActivity;
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

import static com.mediatek.mt6381eco.biz.measure.BaseMeasureViewModel.STATE_CHECKING;
import static com.mediatek.mt6381eco.biz.measure.BaseMeasureViewModel.STATE_COMPLETED;
import static com.mediatek.mt6381eco.biz.measure.BaseMeasureViewModel.STATE_MEASURING;
import static com.mediatek.mt6381eco.biz.measure.InterruptException.TYPE_CONNECTION_LOST;
import static com.mediatek.mt6381eco.biz.measure.InterruptException.TYPE_ERROR;
import static com.mediatek.mt6381eco.biz.measure.InterruptException.TYPE_FINER_OFF;
import static com.mediatek.mt6381eco.biz.measure.InterruptException.TYPE_START_MEASURE_FAIL;

public class CalibrationFragment extends BaseFragment implements Injectable {
  private static final int REQUEST_GOLDEN = 1;
  private static final int REQUEST_SUCCESSFUL_AGAIN = 2;
  private static final int REQUEST_SUCCESSFUL_TO_MEASURE = 3;
  private static final int REQUEST_READY = 5;
  @Inject BaseMeasureViewModel mViewModel;
  @Inject CalibrationViewModel mCalibrationViewModel;
  @Inject CalibrationContract.Presenter mPresenter;
  @Inject SupportSensorTypes mSupportSensorTypes;
  @BindView(R.id.txt_name) TextView mTxtNickName;
  @BindView(R.id.wave_form) RealTimeWaveformView mWaveForm;
  @BindView(R.id.txt_cd_count) TextView mTxtCdCount;
  @BindView(R.id.view_countdown) CountdownPb mCdProgress;
  @BindView(R.id.txt_heart_rate) TextView mTxtHeartRate;
  @BindView(R.id.txt_spo2) TextView mTxtSpo2;
  @BindView(R.id.txt_blood_pressure) TextView mTxtBloodPressure;
  @BindView(R.id.txt_fatigue) TextView mTxtFatigue;
  @BindView(R.id.txt_pressure) TextView mTxtPressure;
  @BindView(R.id.txt_measure_status) TextView mTxtState;
  @BindView(R.id.view_finger_on_off) RelativeLayout mLayoutFingerOnOff;
  @BindView(R.id.txt_finger_msg) TextView mTxtCheckReason;
  @BindView(R.id.btn_finish) Button mBtnFinish;
  @BindView(R.id.btn_cancel) Button mBtnCancel;
  @BindView(R.id.btn_start) Button mBtnStart;
  @BindView(R.id.view_so) View mLayoutSo;
  @BindView(R.id.txt_remeasure) TextView mTxtRemeasure;
  @BindView(R.id.view_chart) LinearLayout mViewChart;
  @BindView(R.id.txt_ekg) TextView mTxtEkg;
  @BindView(R.id.txt_ppg) TextView mTxtPpg;
  @BindView(R.id.txt_ppg2) TextView mTxtPpg2;
  private ServiceBinding.Unbind mServiceUnbinder;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    Log.d("CalibrationFragment","onCreate()");
    super.onCreate(savedInstanceState);
    mServiceUnbinder = ServiceBinding.bindService(this, PeripheralService.class, service -> {
      IPeripheral peripheral = (IPeripheral) service;
      mPresenter.attach(peripheral);
    });
    if (savedInstanceState == null) {
      Log.d("CalibrationFragment","showFirstGolden()");
      //todo by herman for calibration
      showFirstGolden();
    } else {
      restore(savedInstanceState);
    }
  }

  private void restore(Bundle savedInstanceState) {
    CalibrationContract.PresenterState saveState = new CalibrationContract.PresenterState();
    saveState.stateName =
        savedInstanceState.getString(MeasureContract.PresenterState.KEY_STATE_NAME);
    saveState.transObject =
        savedInstanceState.getSerializable(MeasureContract.PresenterState.KEY_TRANS_OBJECT);
    saveState.calibrationData =
        savedInstanceState.getIntArray(CalibrationContract.PresenterState.KEY_CALIBRATION);
    assert saveState.transObject != null;
    mPresenter.restoreSaveState(saveState);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    mServiceUnbinder.unbind();
    mPresenter.destroy();
  }

  @Override protected void initView(Bundle savedInstanceState) {
    int[] sensorTypes = mSupportSensorTypes.getTypeIntArray();
    mWaveForm.initTypes(sensorTypes);
    for (int i = 0; i < mViewModel.waveData.size(); ++i) {
      int key = mViewModel.waveData.keyAt(i);
      mWaveForm.setData(key, mViewModel.waveData.get(key));
    }
    mTxtEkg.setVisibility(
        DataUtils.indexOf(sensorTypes, SensorData.DATA_TYPE_EKG) > -1 ? View.VISIBLE : View.GONE);
    mTxtPpg.setVisibility(
        DataUtils.indexOf(sensorTypes, SensorData.DATA_TYPE_PPG1) > -1 ? View.VISIBLE : View.GONE);
    mTxtPpg2.setVisibility(
        DataUtils.indexOf(sensorTypes, SensorData.DATA_TYPE_PPG2) > -1 ? View.VISIBLE : View.GONE);

    mLayoutSo.setVisibility(View.GONE);
    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mViewChart.getLayoutParams();
    params.weight = 70.0f;
    mViewChart.setLayoutParams(params);

    mViewModel.nickname.observe(this, mTxtNickName::setText);

    mViewModel.progress.observe(this, progress -> {
      mTxtCdCount.setText(String.valueOf(progress));
      mCdProgress.setProgress(progress);
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
      mBtnCancel.setVisibility(
          state == STATE_MEASURING || state == STATE_CHECKING ? View.VISIBLE : View.GONE);
      mBtnFinish.setVisibility(state == STATE_COMPLETED ? View.VISIBLE : View.GONE);
      mBtnStart.setVisibility(state == STATE_COMPLETED ? View.VISIBLE : View.GONE);
    });

    mCalibrationViewModel.goldenInput.observe(this, goldenInput -> {
      if (goldenInput == null || goldenInput < 0) return;
      startActivityForResult(new Intent(getActivity(), CalibrateGoldenActivity.class).putExtra(
          CalibrateGoldenActivity.STEP, goldenInput),
          REQUEST_GOLDEN);
      mCalibrationViewModel.goldenInput.setValue(-1);
    });

    mViewModel.toCheckReason.observe(this, reason -> {
      mLayoutFingerOnOff.setVisibility(reason > 0 ? View.VISIBLE : View.GONE);
      if (reason > 0) {
        mTxtCheckReason.setText(getString(R.string.bad_signal_tips,
            getString(BizUtils.getBadSignalStringIdRes(reason))));
      }
    });
    mViewModel.interruptError.observe(this, throwable -> {
      if (throwable != null) {
        showInterrupt(throwable);
      }
    });
    mCalibrationViewModel.uploadResource.observe(this, resource -> {
      if (resource != null) {
        switch (resource.status) {
          case LOADING: {
            startLoading(getString(R.string.uploading));
            break;
          }
          case SUCCESS: {
            stopLoading();
            mCalibrationViewModel.uploadResource.setValue(null);
            showSuccessfulToMeasure();
            break;
          }
          case ERROR: {
            stopLoading();
            Toast.makeText(getActivity(), ContextUtils.getErrorMessage(resource.throwable),
                Toast.LENGTH_LONG).show();
            mCalibrationViewModel.uploadResource.setValue(null);

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

    mViewModel.remeasure.observe(this, resource -> mTxtRemeasure.setVisibility(
        resource != null && resource.status == Status.LOADING ? View.VISIBLE : View.GONE));

    mViewModel.ekgChecking.observe(this, isChecking -> mTxtEkg.setAlpha(isChecking ? 1f : 0.4f));
    mViewModel.ppg1Checking.observe(this, isChecking -> mTxtPpg.setAlpha(isChecking ? 1f : 0.4f));
    mViewModel.ppg2Checking.observe(this, isChecking -> mTxtPpg2.setAlpha(isChecking ? 1f : 0.4f));
  }

  private void showFirstGolden() {
    startActivityForResult(new Intent(getActivity(), CalibrateGoldenActivity.class).putExtra(
        CalibrateGoldenActivity.STEP, 0), REQUEST_GOLDEN);
  }

  private void showSuccessfulToMeasure() {
    startActivityForResult(
        new Intent(getActivity(), CalibrateSuccess2MeasureActivity.class).putExtra(
            CalibrateSuccess2MeasureActivity.INTENT_NICK_NAME,
            mTxtNickName.getText().toString()), REQUEST_SUCCESSFUL_TO_MEASURE);
  }

  private void showInterrupt(Throwable throwable) {
    if (throwable != null && throwable instanceof InterruptException) {
      MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity()).cancelable(false);
      switch (((InterruptException) throwable).type) {
        case TYPE_CONNECTION_LOST: {
          builder.title(R.string.calibrate_interrupted)
              .content(R.string.connection_lost)
              .positiveText(R.string.exit)
              .dismissListener(dialog -> getActivity().finish());
          break;
        }
        case TYPE_FINER_OFF: {
          builder.title(R.string.calibrate_interrupted)
              .content(R.string.calibrate_error)
              .negativeText(R.string.no)
              .onNegative((dialog, which) -> getActivity().finish())
              .positiveText(R.string.measure_interrupt_yes)
              .onPositive((dialog, which) -> showCalibrationReady());
          break;
        }
        case TYPE_START_MEASURE_FAIL: {
          builder.title(R.string.calibrate_interrupted)
              .content(R.string.error_start_measure_fail)
              .positiveText(R.string.exit)
              .dismissListener(dialog -> getActivity().finish());
          break;
        }
        case TYPE_ERROR: {
          builder.title(R.string.calibrate_interrupted)
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

  private void navToMeasure() {
    getActivity().startActivity(new Intent(getActivity(), MeasureActivity.class));
    getActivity().finish();
  }

  private void showCalibrationReady() {
    startActivityForResult(
        new Intent(getActivity(), MeasureReadyActivity.class).putExtra(MeasureReadyActivity.FROM,
            MeasureReadyActivity.FROM_CALIBRATION), REQUEST_READY);
  }

  @OnClick(R.id.btn_finish)
  void onFinishClick() {
    getActivity().finish();
  }

  @OnClick(R.id.btn_cancel)
  void onBtnCancelClick() {
    new MaterialDialog.Builder(getActivity()).content(R.string.cancel_calibrate)
        .positiveText(R.string.yes)
        .negativeText(R.string.no)
        .cancelable(false)
        .onPositive((dialog, which) -> getActivity().finish())
        .show();
  }

  @OnClick(R.id.btn_start)
  void onBtnStartClick() {
    mPresenter.reset();
    showCalibrationReady();
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
      case REQUEST_GOLDEN: {
        if (resultCode == Activity.RESULT_OK) {
          int sbp = data.getIntExtra(CalibrateGoldenActivity.DATA_SBP, 0);
          int dbp = data.getIntExtra(CalibrateGoldenActivity.DATA_DBP, 0);
          int hr = data.getIntExtra(CalibrateGoldenActivity.DATA_HR, 0);
          int step = data.getIntExtra(CalibrateGoldenActivity.STEP, 0);
          Log.d("CalibrationFragment","sbp: " + sbp);
          Log.d("CalibrationFragment","dbp: " + dbp);
          Log.d("CalibrationFragment","hr: " + hr);
          Log.d("CalibrationFragment","step: " + step);
          mPresenter.inputGolden(step, sbp, dbp, hr);
          switch (step) {
            case 0: {
              showCalibrationReady();
              break;
            }
            case 1: {
              startActivityForResult(
                  new Intent(getActivity(), CalibrateSuccessActivity.class).putExtra(
                      CalibrateSuccessActivity.INTENT_NICK_NAME,
                      mTxtNickName.getText().toString()), REQUEST_SUCCESSFUL_AGAIN);
              break;
            }
            case 2: {
              mPresenter.uploadCalibration();
              break;
            }
          }
        } else {
          getActivity().finish();
        }
        break;
      }
      case REQUEST_SUCCESSFUL_AGAIN: {
        if (CalibrateSuccessActivity.RESULT_AGAIN == resultCode) {
          showCalibrationReady();
        } else {
          mPresenter.uploadCalibration();
        }
        break;
      }
      case REQUEST_SUCCESSFUL_TO_MEASURE: {
        if (Activity.RESULT_OK == resultCode) {
          navToMeasure();
        } else {
          getActivity().finish();
        }
        break;
      }
      case REQUEST_READY: {
        if (Activity.RESULT_OK == resultCode) {
          mPresenter.startMeasure(
              data != null && data.getBooleanExtra(MeasureReadyActivity.DATA_DOWN_SAMPLE, false));
        } else {
          getActivity().finish();
        }
      }
    }
  }

  @OnLongClick(R.id.txt_ekg) boolean onTxtEkgClick() {
    mPresenter.toggleEKGChecking();
    return true;
  }

  @OnLongClick(R.id.txt_ppg) boolean onTxtPPG1Click() {
    mPresenter.togglePPG1Checking();
    return true;
  }

  @OnLongClick(R.id.txt_ppg2) boolean onTxtPPG2Click() {
    mPresenter.togglePPG2Checking();
    return true;
  }

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_measure2, container, false);
  }

  @Override public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    CalibrationContract.PresenterState saveState =
        (CalibrationContract.PresenterState) mPresenter.getSaveState();
    outState.putString(MeasureContract.PresenterState.KEY_STATE_NAME, saveState.stateName);
    outState.putSerializable(MeasureContract.PresenterState.KEY_TRANS_OBJECT,
        saveState.transObject);
    outState.putIntArray(CalibrationContract.PresenterState.KEY_CALIBRATION,
        saveState.calibrationData);
  }

  @Override public void onStop() {
    super.onStop();
    if (!getActivity().isChangingConfigurations()) {
      mPresenter.abort();
    }
  }
}
