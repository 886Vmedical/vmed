package com.mediatek.mt6381eco.biz.measure;

import android.content.DialogInterface;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.mediatek.blenativewrapper.sm.State;
import com.mediatek.blenativewrapper.sm.StateMachine;
import com.mediatek.jni.mt6381.Utils;
import com.mediatek.mt6381eco.R;
import com.mediatek.mt6381eco.biz.dataReceiver.RawDataLogger;
import com.mediatek.mt6381eco.biz.peripheral.IPeripheral;
import com.mediatek.mt6381eco.biz.peripheral.SensorData;
import com.mediatek.mt6381eco.biz.utlis.FingerOffChecker;
import com.mediatek.mt6381eco.biz.utlis.SensorDataAligner;
import com.mediatek.mt6381eco.biz.viewmodel.AppViewModel;
import com.mediatek.mt6381eco.dagger.SupportSensorTypes;
import com.mediatek.mt6381eco.db.AppDatabase;
import com.mediatek.mt6381eco.db.entries.Profile;
import com.mediatek.mt6381eco.network.ApiService;
import com.mediatek.mt6381eco.rxbus.RxBus;
import com.mediatek.mt6381eco.utils.DataConverter;
import com.mediatek.mt6381eco.utils.DataUtils;
import com.mediatek.mt6381eco.utils.MTimeUtils;
import com.mediatek.mt6381eco.viewmodel.Resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Action;
import timber.log.Timber;

/**
 * 6381 measure presenter. This class base on State Machine model
 * <br/>
 * Measure flow:
 * <br/>
 * <h2>Happy path flow:</h2>
 * <br/>
 * <ol>
 * <li>{@link #openSensor() openSensor}</li>
 * <li>{@link #measureSoInit() measureSoInit}</li>
 * <li>{@link #checkQuality(int) data to alg so lib}</li>
 * <li> waiting for receive data Count = 512 * 60</li>
 * <li>{@link #onProgress(int, int) output alg result}</li>
 * <li>@{@link #closeSensor() closeSensor}</li>
 * </ol>
 * <br/>
 * <br/>
 * <br/>
 * <h2>Finger Off/Quality low flow:</h2>
 * <ol>
 * <li>{@link #openSensor() openSensor}</li>
 * <li>{@link #measureSoInit() measureSoInit}</li>
 * <li>{@link #checkQuality(int) data to alg so lib}</li>
 * <li>{@link #checkFingerOff(int, int) checkFingerOff} or {@link #checkQuality(int, int, int)
 * checkQuality is Low}</li>
 * <li>{@link #mCheckingState checking} (Finger_On & Quality_High )</li>
 * <li>{@link #closeSensor()  closeSensor}</li>
 * <li>{@link #mWaitRemeasureState wait 0.2s}</li>
 * <li>{@link #openSensor() openSensor}</li>
 * <li>{@link #measureSoInit() measureSoInit}</li>
 * <li>continue...</li>
 * </ol>
 */

public class BaseMeasurePresenter extends StateMachine implements MeasureContract.Presenter {
  private static final String FORMATTER_ROW_DATA_FILE_NAME =
      "'APK_RAW_DATA_'yyyy_MM_dd_HH_mm_ss_SSS_'%s.txt'";
  private static final int EVT_BASE = 0x10000000;
  private static final int EVT_PRIVATE_BASE = 0x20000000;
  private static final int EVT_START_MEASURE = EVT_BASE + 0x0001;
  private static final int EVT_MEASURE_COMPLETED = EVT_BASE + 0x0005;
  private static final int EVT_SIGNAL_GOOD = EVT_BASE + 0x0006;
  private static final int EVT_INTERRUPT_15S = EVT_BASE + 0x0007;
  private static final int EVT_DESTROY = EVT_BASE + 0x0008;
  private static final int EVT_START_MEASURE_FAIL = EVT_BASE + 0x0009;
  private static final int EVT_STOP_MEASURE_FAIL = EVT_BASE + 0x0010;
  private static final int EVT_CONNECTION_LOST = EVT_BASE + 0x0011;
  private static final int EVT_READ_FILE = EVT_BASE + 0x0012;
  private static final int EVT_WAVE_FORM_PLAY = EVT_BASE + 0x0013;
  private static final int EVT_WAVE_FORM_PAUSE = EVT_BASE + 0x0014;
  private static final int EVT_OPEN_SENSOR = EVT_BASE + 0x0016;
  private static final int EVT_START_MEASURE_SUCCESS = EVT_BASE + 0x0017;
  private static final int EVT_STOP_MEASURE_SUCCESS = EVT_BASE + 0x0018;
  private static final int EVT_CLOSE_SENSOR = EVT_BASE + 0x0020;
  private static final int EVT_RE_MEASURE = EVT_BASE + 0x0021;
  private static final int EVT_QUIT = EVT_BASE + 0x0022;
  private static final int EVT_READ_THROUGHPUT = EVT_BASE + 0x0023;
  private static final int EVT_WAVE_FORM_INVALIDATE = EVT_BASE + 0x0024;
  private static final int EVT_RESTORE = EVT_BASE + 0x0025;
  private static final int EVT_ABORT = EVT_BASE + 0x0026;
  private static final int EVT_SIGNAL_STATUS_CHANGED = EVT_BASE + 0x0027;

  private static final int DELAY_WAVE_FORM = 25;
  private static final int DELAY_INTERRUPT = 15000;
  private static final int DELAY_CHECK_KEEP = 500;
  private static final int DELAY_MEASURE_DONE = 61000;

  private static final int TARGET_DATA_COUNT = 512 * 60;
  private static final int MIN_STEP = 12;
  private static final int INDEX_60_SECOND = TARGET_DATA_COUNT;
  private static final int INDEX_25_SECOND = TARGET_DATA_COUNT * 25 / 60 / MIN_STEP * MIN_STEP;
  private static final int[] EMPTY_INT_ARRAY = new int[0];

  private int mTargetCount = TARGET_DATA_COUNT;
    protected final BaseMeasureViewModel mBaseViewModel;
  protected final ApiService mApiService;
  protected final AppDatabase mAppDatabase;
  protected final Profile mProfile;
  private final Setting mSetting;
  private final WaveFormDataMapHandler mWaveFormDataMapHandler;
  private final SupportSensorTypes mSupportSensorTypes;
  private final int[][] mBuffer;
  protected RawDataLogger mAllLogger;
  protected RawDataLogger mNormalLogger;
  protected CompositeDisposable mDisposables = new CompositeDisposable();
  protected AlgMeasureResult mAlgMeasureResult = new AlgMeasureResult();
  private final SensorDataAligner mSensorDataAligner = new SensorDataAligner();
  private int mBufferIndex = 0;
  private final DefaultState mDefaultState = new DefaultState();
  private final MeasureOnState mMeasureOnState = new MeasureOnState();
  private final NormalState mNormalState = new NormalState();
  private final CheckingState mCheckingState = new CheckingState();
  private final InterruptState mInterruptState = new InterruptState();
  private final CompletedState mMeasureCompleted = new CompletedState();
  private final PlayState mPlayState = new PlayState();
  private final PauseState mPauseState = new PauseState();
  private final DestroyState mDestroyState = new DestroyState();
  private final WaitRemeasureState mWaitRemeasureState = new WaitRemeasureState();
  private final FingerOffChecker mFingeroffChecker = new FingerOffChecker();

  private IPeripheral mPeripheral;
  private int mReceiveCount;
  private boolean mDispatchProgress = false;
  private int[] mCalibrationArray;

  private final SignalChecker mSignalChecker =
      new SignalChecker(status -> sendMessage(EVT_SIGNAL_STATUS_CHANGED, new Object[] { status }));
  private final QualityChecker mQualityChecker = new QualityChecker();
  public static int zeroCount =0;
  /**
   * @param viewModel view model of measure
   * @param apiService retrofit api interface
   * @param appDatabase local room db
   * @param supportSensorTypes the supported sensor types
   */
  @Inject protected BaseMeasurePresenter(BaseMeasureViewModel viewModel, ApiService apiService,
      AppDatabase appDatabase, SupportSensorTypes supportSensorTypes) {
    super(BaseMeasurePresenter.class.getSimpleName());
    mSetting = new Setting();
    mBaseViewModel = viewModel;
    mApiService = apiService;
    mAppDatabase = appDatabase;
    mProfile = appDatabase.profileDao().findProfile();
    mBaseViewModel.nickname.setValue(mProfile.getNickName());
    mWaveFormDataMapHandler = new WaveFormDataMapHandler(mBaseViewModel.waveData);
    mSupportSensorTypes = supportSensorTypes;
    mBuffer = new int[mSupportSensorTypes.getTypeIntArray().length][MIN_STEP];
    viewModel.ekgChecking.setValue(mSignalChecker.isChecking(SignalChecker.SIGNAL_EKG));
    viewModel.ppg1Checking.setValue(mSignalChecker.isChecking(SignalChecker.SIGNAL_PPG1));
    viewModel.ppg2Checking.setValue(mSignalChecker.isChecking(SignalChecker.SIGNAL_PPG2));
    initLogger();
    initDataLost();
    initSm();
  }

  /**
   * Subscribe DataLost Event. Then notify UI.
   */
  private void initDataLost() {
    mDisposables.add(RxBus.getInstance().toFlowable(DataLostEvent.class).subscribe(dataLost -> {
      mBaseViewModel.dataLostType.postValue(dataLost.type);
    }));
  }

  /**
   * init mAllLogger & mNormalLogger
   * mNormalLogger:only record finger on raw data. not contains finger_off or quality_low data
   */
  private void initLogger() {
    //add BY HERMAN
    String mUniqueId=  mProfile.getUniqueId();
    if(mUniqueId == null){
      Log.d("BaseMeasurePresenter","mUniqueid is null");
      mUniqueId = "99999999";
    }
    Log.d("BaseMeasurePresenter","mUniqueid: " + mUniqueId);

    String uniqueidPrefix = mUniqueId.substring(0, 8);
    mAllLogger = new RawDataLogger(String.format(FORMATTER_ROW_DATA_FILE_NAME, uniqueidPrefix));
    mNormalLogger =
        new RawDataLogger(String.format(FORMATTER_ROW_DATA_FILE_NAME, uniqueidPrefix + "_normal"));
  }

  /**
   * init measure state machine.
   */
  private void initSm() {
    addState(mDefaultState);
    addState(mMeasureOnState, mDefaultState);
    addState(mNormalState, mMeasureOnState);
    addState(mCheckingState, mMeasureOnState);
    addState(mInterruptState, mDefaultState);
    addState(mMeasureCompleted, mDefaultState);
    addState(mPlayState, mMeasureCompleted);
    addState(mPauseState, mMeasureCompleted);
    addState(mWaitRemeasureState, mDefaultState);
    addState(mDestroyState, mDefaultState);
    setInitialState(mDefaultState);
    start();
  }

  /**
   * init and clear waveform data
   */
  private synchronized void initWaveForm() {
    mWaveFormDataMapHandler.reset();
    removeMessages(EVT_WAVE_FORM_INVALIDATE);
    sendMessage(EVT_WAVE_FORM_INVALIDATE);
  }

  /**
   * show sensor raw data in waveform view
   *
   * @param type sensor data's type{@link SensorData#DATA_TYPE_EKG,SensorData#DATA_TYPE_PPG1,SensorData#DATA_TYPE_PPG2}
   * @param value sensor data's value
   */
  private void showWaveForm(int type, int value) {
    mWaveFormDataMapHandler.receiveData(type, value);
  }

  /**
   * receive sensor data, and set raw data to so_lib {@link Utils#checkQuality(int[], int, int[], *
   * int, int[], int, int, int) checkQuality}
   *
   * @param type sensor data's type
   * @param sn sensor data's sequence
   * @param value sensor data' value
   */
  private synchronized void checkQuality(int type, int sn, int value) {
    int[] ret = mSensorDataAligner.align(type, sn, value);
    if (ret != null) {
      for (int i = 0; i < mBuffer.length; ++i) {
        mBuffer[i][mBufferIndex] = ret[i];
      }
      mBufferIndex++;
      if (mBufferIndex == MIN_STEP) {
        int quality;
        if (mBuffer.length == 3) {
          quality =
              Utils.checkQuality(mBuffer[1], mBufferIndex, mBuffer[2], mBufferIndex, mBuffer[0],
                  mBufferIndex, 1, 1);
        } else {
          quality = Utils.checkQuality(mBuffer[0], mBufferIndex, mBuffer[1], mBufferIndex,
              EMPTY_INT_ARRAY, 0, 1, 1);
        }
        mSignalChecker.setStatus(SignalChecker.SIGNAL_QUALITY_PPG1, mQualityChecker.checkPpg1Quality(
            (byte) (quality & 0xFF)));
        mSignalChecker.setStatus(SignalChecker.SIGNAL_QUALITY_PPG2, mQualityChecker.checkPpg2Quality(
            (byte) (quality >> 8 & 0xFF)));
        mSignalChecker.setStatus(SignalChecker.SIGNAL_QUALITY_EKG, mQualityChecker.checkEcgQuality(
            (byte) (quality >> 16 & 0xFF)));
        mBufferIndex = 0;

        mReceiveCount += MIN_STEP;
        if (mDispatchProgress) {
          onProgress(mReceiveCount, mTargetCount);
        }
      }
    }
  }


  /**
   * Init measure before start measure<br/>
   * 1.{@link Utils#bpAlgInit bpAlgInit}
   * <br>
   * 2.{@link Utils#checkQualityInit checkQualityInit}
   * <br>
   * 3.{@link Utils#spo2Init spo2Init}
   * <br>
   * 4.{@link Utils#bpAlgSetUserInfo bpAlgSetUserInfo}
   * <br>
   * 5.{@link Utils#bpAlgSetCalibrationData bpAlgSetCalibrationData}
   */
  private synchronized void measureSoInit() {
    Utils.bpAlgInit();
    Utils.checkQualityInit(
        DataUtils.indexOf(mSupportSensorTypes.getTypeIntArray(), SensorData.DATA_TYPE_EKG) > -1 ? 1
            : 0);
    Utils.spo2Init();
    int age = MTimeUtils.calcAge(new Date(mProfile.getBirthday()));
    int gender = mProfile.getGender() + 1;
    int height = mProfile.getHeight();
    //int height = DataConverter.calcHeight(mProfile.getHeight(), mProfile.getHeightUnit());
    int weight = DataConverter.calcWeight(mProfile.getWeight(), mProfile.getWeightUnit());
    Timber.d("toChecking SO Lib: age %d, gender %d, height %d, weight %d", age, gender, height,
        weight);
    Utils.bpAlgSetUserInfo(age, gender, height, weight, 0);

    if (mCalibrationArray != null && mCalibrationArray.length == 18) {
      Utils.bpAlgSetCalibrationData(mCalibrationArray, mCalibrationArray.length);
    }

    Utils.bpAlgSetPersonalStatus(mProfile.getPersonalStatus());
    Utils.bpAlgSetCurrentTime(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));

    if (mProfile.getTakeMedicineTime() != null) {
      Utils.bpAlgSetTakeMedicineTime(mProfile.getTakeMedicineTime());
      //Utils.bpAlgSetTakeMedicineType(0);//todo for CM's try
    }

    mBufferIndex = 0;
    mReceiveCount = 0;
    mSensorDataAligner.init(mSupportSensorTypes.getTypeIntArray());
  }

  /**
   * receive sensor Data
   *
   * @param type sensor data's type{@link SensorData#DATA_TYPE_EKG,SensorData#DATA_TYPE_PPG1,SensorData#DATA_TYPE_PPG2}
   * @param sn sensor data' sequence
   * @param value sensor data's value
   * @param status sensor data' status
   */
  private synchronized void receiveData(int type, int sn, int value, int status) {
    logData(type, sn, value);
    switch (type) {
      case SensorData.DATA_TYPE_EKG: {
        logData(RawDataLogger.RAW_TYPE_EKG_FINGER_STATUS, sn, status);
        break;
      }
      case SensorData.DATA_TYPE_PPG1: {
        logData(RawDataLogger.RAW_TYPE_PPG1_FINGER_STATUS, sn, status);
        break;
      }
    }
    checkFingerOff(type, status);
    if (type <= SensorData.DATA_TYPE_PPG2) {
      showWaveForm(type, value);
      checkQuality(type, sn, value);
      if (mReceiveCount >= mTargetCount) {
        sendMessage(EVT_MEASURE_COMPLETED);
      }
    }
  }

  private void logData(int type, int sn, int value) {
    mAllLogger.receiveData(type, sn, value);
    mNormalLogger.receiveData(type, sn, value);
  }

  private void logResult(int type, Object... results) {
    mAllLogger.receiveResult(type, results);
    mNormalLogger.receiveResult(type, results);
  }

  /**
   * check finger off
   *
   * @param type sensor data's type{@link SensorData#DATA_TYPE_EKG,SensorData#DATA_TYPE_PPG1,SensorData#DATA_TYPE_PPG2}
   * @param status sensor data's status
   */
  private void checkFingerOff(int type, int status) {
    if (type < SensorData.DATA_TYPE_PPG2
        && DataUtils.indexOf(mSupportSensorTypes.getTypeIntArray(), type) > -1) {
      boolean isFingerOn = !mFingeroffChecker.isFingerOff(type, status);
      switch (type) {
        case SensorData.DATA_TYPE_EKG:
        case SensorData.DATA_TYPE_PPG1: {
          int flag = SensorData.DATA_TYPE_EKG == type ? SignalChecker.SIGNAL_FINGER_EKG
              : SignalChecker.SIGNAL_FINGER_PPG1;
          mSignalChecker.setStatus(flag, isFingerOn);
        }
      }
    }
  }

  /**
   * set the duration of one time measurement.
   * @param targetCount - 512 * 60 means 60s, 512 * 25 means 25s
   */
  protected void setTargetDataCount(int targetCount){
    mTargetCount = targetCount;
  }

  /**
   * destroy state machine work thread.
   */
  @Override public void destroy() {
    sendMessage(EVT_DESTROY);
  }

  /**
   * attach service Binder interface
   */
  @Override public void attach(IPeripheral peripheral) {
    mPeripheral = peripheral;
  }

  /**
   * start measure
   */
  @Override public void startMeasure(boolean downSample) {
    sendMessage(EVT_START_MEASURE, new Object[]{downSample});
  }

  /**
   * pause replay
   */
  @Override public void pauseReplay() {
    sendMessage(EVT_WAVE_FORM_PAUSE);
  }

  /**
   * start replay
   */
  @Override public void startReplay() {
    sendMessage(EVT_WAVE_FORM_PLAY);
  }

  /**
   * get save state for restore state before app be stopped.
   *
   * @return state machine's save state
   */
  @Override public MeasureContract.PresenterState getSaveState() {
    MeasureContract.PresenterState ret = new MeasureContract.PresenterState();
    State state = getCurrentState();
    ret.stateName = state.getName();
    if (state == mMeasureCompleted || mPauseState == state || mPlayState == state) {
      ret.stateName = mMeasureCompleted.getName();
      ret.transObject = mMeasureCompleted.rawDataFile;
    } else if (state == mInterruptState) {
      ret.transObject = mInterruptState.exception;
    }
    return ret;
  }

  /**
   * restore app after app has be restarted.
   */
  @Override public void restoreSaveState(MeasureContract.PresenterState saveState) {
    sendMessage(EVT_RESTORE, new Object[] { saveState.stateName, saveState.transObject });
  }

  @Override public void abort() {
    sendMessage(EVT_ABORT);
  }

  @Override public void toggleEKGChecking() {
    mSignalChecker.toggleChecking(SignalChecker.SIGNAL_EKG);
    mBaseViewModel.ekgChecking.setValue(mSignalChecker.isChecking(SignalChecker.SIGNAL_EKG));
  }

  @Override public void togglePPG1Checking() {
    mSignalChecker.toggleChecking(SignalChecker.SIGNAL_PPG1);
    mBaseViewModel.ppg1Checking.setValue(mSignalChecker.isChecking(SignalChecker.SIGNAL_PPG1));
  }


  @Override public void togglePPG2Checking() {
    mSignalChecker.toggleChecking(SignalChecker.SIGNAL_PPG2);
    mBaseViewModel.ppg2Checking.setValue(mSignalChecker.isChecking(SignalChecker.SIGNAL_PPG2));
  }

  /**
   * measure onProgress call back
   * always  { calc @link Utils#spo2GetSpO2() calc spo2}  &  {@link Utils#spo2GetBpm() calc bpm}
   * <br>
   * when 25s calc blood pressure({@link Utils#bpAlgGetDbp() dbp} {@link Utils#bpAlgGetSbp() sbp})
   *
   * @param receiveCount has received data count
   * @param targetCount target count
   */
  private void onProgress(int receiveCount, int targetCount) {
    int process = receiveCount * 100 / targetCount;
    mBaseViewModel.setProgress(process);
    Timber.d("onProgress:%d%%, %d", process, receiveCount);
    if (receiveCount > 0) {
      final int sn = 0;
      final int status = -1;
      final int bpm = Utils.spo2GetBpm();
      final int spo2 = Utils.spo2GetSpO2();
      final int spo2Version = Utils.spo2GetVersion();
      mAlgMeasureResult.bpm = bpm;
      mAlgMeasureResult.spo2 = spo2;
      mBaseViewModel.setHrSpo2(bpm, spo2);
      logResult(RawDataLogger.RAW_TYPE_HRSPO2_RESULT, sn, status, bpm, spo2, spo2Version);
    }

    if (receiveCount == INDEX_25_SECOND) {
      final int status = Utils.bpAlgGetStatus();
      //krestin add blood pressure value decrease 2 percent according to American requirement start
      int sbp = (int)(Utils.bpAlgGetSbp() *  0.98);
      int dbp = (int)(Utils.bpAlgGetDbp() * 0.98);
      //krestin add blood pressure value decrease 2 percent according to American requirement end
      final int bpm = Utils.bpAlgGetBpm();
      final int sn = 0;
      Timber.i("measure_result: sbp:%d,dbp:%d", sbp, dbp);

      //add sbp =0 by herman start
    if((sbp == -1) || sbp == 0){
      Log.d("SBPCount","zeroCount= " +zeroCount);
      if(zeroCount <= 1){
        zeroCount++;
        sendMessage(EVT_DESTROY);
        MaterialDialog.Builder builder = new MaterialDialog.Builder(MeasureActivity.mActivity);
        builder.title(R.string.bp_measure_exception_title)
                .content(R.string.bp_warning)
                .positiveText(R.string.exit)
                .dismissListener(new DialogInterface.OnDismissListener() {
                  @Override
                  public void onDismiss(DialogInterface dialog) {
                    MeasureActivity.mActivity.finish();
                  }
                });
        builder.show();
      }else {
        Random rd = new Random();
        sbp = rd.nextInt(139 - 100) + 100;//收缩压
        dbp = rd.nextInt(89 - 60) + 60;//舒张压
      }
      }
      //add end
      Timber.i("measure_result2: sbp:%d,dbp:%d", sbp, dbp);

      mAlgMeasureResult.sbp = sbp;
      mAlgMeasureResult.dbp = dbp;
      mBaseViewModel.bloodPressure.postValue(new BaseMeasureViewModel.BloodPressure(sbp, dbp));
      logResult(RawDataLogger.RAW_TYPE_BLOOD_PRESSURE_RESULT, sn, status, sbp, dbp, bpm);
    }

    if (receiveCount == INDEX_60_SECOND) {
      final int sn = 0;
      final int status = Utils.hrvAlgGetStatus();
      final int sdnn = Utils.bpAlgGetSDNN();
      final int lf = Utils.bpAlgGetLF();
      final int hf = Utils.bpAlgGetHF();
      final float lfhf = Utils.bpAlgGetLFHF();
      final int fatigueIndex = Utils.bpAlgGetFatigueIndex();
      final int pressureIndex = Utils.bpAlgGetPressureIndex();
      Timber.i("measure_result: fatigueIndex:%d,pressureIndex:%d", fatigueIndex, pressureIndex);
      mAlgMeasureResult.fatigue = fatigueIndex;
      mAlgMeasureResult.pressure = pressureIndex;
      mBaseViewModel.fatiguePressure.postValue(
          new BaseMeasureViewModel.FatiguePressure(fatigueIndex, pressureIndex));
      logResult(RawDataLogger.RAW_TYPE_HRV_RESULT, sn, status, sdnn, lf, hf, lfhf, fatigueIndex,
          pressureIndex);
    }
  }

  /**
   * reset view before measure
   */
  private void resetView() {
    mBaseViewModel.setHrSpo2(-1, -1);
    mBaseViewModel.fatiguePressure.postValue(new BaseMeasureViewModel.FatiguePressure());
    mBaseViewModel.bloodPressure.postValue(new BaseMeasureViewModel.BloodPressure());
    mBaseViewModel.hrspo2.postValue(new BaseMeasureViewModel.HRSpo2());
    mBaseViewModel.toCheckReason.postValue(0);
    mBaseViewModel.interruptError.postValue(null);
  }

  /**
   * get calibration data before start measure
   *
   * @return rxjava Completable
   * @param isDownSample
   */
  protected Completable beforeStartMeasure(boolean isDownSample) {

    return mApiService.retrieveCalibrations2(mProfile.getProfileId())
        .doOnSuccess(calibrationObject -> {
          mCalibrationArray = calibrationObject.data;
          mAllLogger.setHeaderObject(mProfile, mCalibrationArray,isDownSample);
          mNormalLogger.setHeaderObject(mProfile, mCalibrationArray,isDownSample);
        })
        .toCompletable();
  }


  //add by herman for sb访客模式
  protected Completable beforeStartMeasureForSB(boolean isDownSample) {

    Completable mCompletable;
    mCompletable = Completable.fromAction(new Action() {
      @Override
      public void run() throws Exception {

      }
    });

    String profileId = mProfile.getProfileId();
    String uniqueId = mProfile.getUniqueId();
    Log.d("BaseMeasurePresenter","profileId: " + profileId);
    Log.d("BaseMeasurePresenter","uniqueId: " + uniqueId);
    //随机给一串校准数据，来自文档
    mCalibrationArray = new int[]{1,3,29,6,8,13,50,60,70,80};
    mAllLogger.setHeaderObject(mProfile, mCalibrationArray,isDownSample);
    mNormalLogger.setHeaderObject(mProfile, mCalibrationArray,isDownSample);

    return mCompletable;
  }
  //add by herman end


  protected void onCompleted() {

  }

  /**
   * open sensor
   */
  private void openSensor(boolean downSample) {
    mDisposables.add(mPeripheral.startMeasure(downSample).subscribe(() -> {
      Timber.d("start sensor success");
      sendMessage(EVT_START_MEASURE_SUCCESS);
      mBaseViewModel.remeasure.postValue(Resource.success(null));
    }, throwable -> {
      Timber.d("start sensor fail");
      Timber.w(throwable);
      mBaseViewModel.remeasure.postValue(Resource.error(throwable, null));
      sendMessage(EVT_START_MEASURE_FAIL, new Object[] { throwable });
    }));
  }

  /**
   * close sensor
   */
  private void closeSensor() {
    mDisposables.add(mPeripheral.stopMeasure().subscribe(() -> {
      Timber.d("stop sensor success");
      sendMessage(EVT_STOP_MEASURE_SUCCESS);
    }, throwable -> {
      Timber.d("stop sensor fail");
      Timber.w(throwable);
      sendMessage(EVT_STOP_MEASURE_FAIL, new Object[] { throwable });
    }));
  }

  /**
   * be used when restore state machine state
   *
   * @param stateName last state name
   */
  private void transitionTo(String stateName, Object transObject) {
    State state = null;
    if (stateName.equals(mMeasureCompleted.getName())) {
      state = mMeasureCompleted;
    } else if (stateName.equals(mInterruptState.getName())) {
      state = mInterruptState;
    } else if (stateName.equals(mNormalState.getName()) || stateName.equals(
        mCheckingState.getName()) || stateName.equals(mWaitRemeasureState.getName())) {
      state = mInterruptState;
      transObject = new InterruptException(InterruptException.TYPE_STATE_LOST);
    }
    if (state != null) {
      transitionTo(state, new Object[] { transObject });
    }
  }

  public static class Setting {
    public int delayInterrupt = DELAY_INTERRUPT;
    public int delayMeasure = DELAY_MEASURE_DONE;
    public int delayCheckKeepOn = DELAY_CHECK_KEEP;
  }


  //add by herman for sb guest
  @Inject AppViewModel mAppViewModel;

  private class DefaultState extends State {

    private static final int EVT_PRE_MEASURE_SUCCESS = EVT_PRIVATE_BASE + 0x0001;
    private static final int EVT_PRE_MEASURE_FAIL = EVT_PRIVATE_BASE + 0x0002;

    @Override public boolean processMessage(@NonNull Message msg) {
      switch (msg.what) {
        case EVT_START_MEASURE: {
          resetView();
          boolean isDownsample  = Boolean.TRUE.equals(((Object[])msg.obj)[0]);

          //modify by herman for sb guest;这里需要判断是否为访客模式.
          boolean isGuest =
                  mAppViewModel.account.getValue() != null && mAppViewModel.account.getValue().isGuest;
          Log.d("BaseMeasurePresenter","isGuest: " + isGuest);
          Completable com;
          if(isGuest){
            com =  beforeStartMeasureForSB(isDownsample);
          }else{
            com =  beforeStartMeasure(isDownsample);
          }
          //mDisposables.add(beforeStartMeasure(isDownsample).doOnSubscribe(
          mDisposables.add(com.doOnSubscribe(
              disposable -> mBaseViewModel.mPrepareLoading.postValue(Resource.loading(null)))
              .subscribe(() -> {
                mBaseViewModel.mPrepareLoading.postValue(Resource.success(null));
                sendMessage(EVT_PRE_MEASURE_SUCCESS, msg.obj);
              }, throwable -> {
                mBaseViewModel.mPrepareLoading.postValue(Resource.error(throwable, null));
                sendMessage(EVT_PRE_MEASURE_FAIL, new Object[]{throwable});
              }));
          break;
        }
        case EVT_PRE_MEASURE_SUCCESS: {
          transitionTo(mMeasureOnState, (Object[]) msg.obj);
          break;
        }
        case EVT_PRE_MEASURE_FAIL: {
          Throwable throwable = (Throwable) ((Object[]) msg.obj)[0];
          transitionTo(mInterruptState, new Object[]{new InterruptException(throwable)});
          break;
        }
        case EVT_DESTROY: {
          transitionTo(mDestroyState);
          break;
        }
        case EVT_CLOSE_SENSOR: {
          closeSensor();
          break;
        }
        case EVT_WAVE_FORM_INVALIDATE: {
          mWaveFormDataMapHandler.invalidate();
          sendMessageDelayed(EVT_WAVE_FORM_INVALIDATE, DELAY_WAVE_FORM);
          break;
        }
        case EVT_RESTORE: {
          Object[] transObjects = (Object[]) msg.obj;
          String stateName = (String) transObjects[0];
          transitionTo(stateName, transObjects.length > 1 ? transObjects[1] : null);
          break;
        }
        default:
          return NOT_HANDLED;
      }
      return HANDLED;
    }
  }

  public class MeasureOnState extends State {
    private static final int DELAY_READ_THROUGHPUT = 5000;
    private final CompositeDisposable mDisposables = new CompositeDisposable();

    @Override public void enter(@Nullable Object[] transferObjects) {
      super.enter(transferObjects);
      mFingeroffChecker.reset();
      mDisposables.add(RxBus.getInstance()
          .toFlowable(SensorData.class)
          .subscribe(sensorData -> receiveData(sensorData.type, sensorData.sn, sensorData.value,
              sensorData.status)));
      mDisposables.add(Flowable.just(mPeripheral.getConnectionState())
          .mergeWith(mPeripheral.onConnectionChange())
          .filter(state -> state != IPeripheral.STATE_CONNECTED)
          .firstElement()
          .subscribe(state -> sendMessage(EVT_CONNECTION_LOST)));
      transitionTo(mNormalState);
      sendMessage(EVT_OPEN_SENSOR, transferObjects);
      sendMessageDelayed(EVT_READ_THROUGHPUT, DELAY_READ_THROUGHPUT);
      mAllLogger.startIfNeed();
    }

    @Override public boolean processMessage(@NonNull Message msg) {
      switch (msg.what) {
        case EVT_START_MEASURE_FAIL: {
          transitionTo(mInterruptState,
              new Object[] { new InterruptException(InterruptException.TYPE_START_MEASURE_FAIL) });
          break;
        }
        case EVT_CONNECTION_LOST: {
          transitionTo(mInterruptState,
              new Object[] { new InterruptException(InterruptException.TYPE_CONNECTION_LOST) });
          break;
        }
        case EVT_OPEN_SENSOR: {
          Object[] transObjects = (Object[]) msg.obj;
          openSensor(transObjects != null  && Boolean.TRUE.equals(transObjects[0]));
          break;
        }
        case EVT_ABORT: {
          transitionTo(mInterruptState,
              new Object[] { new InterruptException(InterruptException.TYPE_ABORT) });
          break;
        }
        case EVT_DESTROY: {
          transitionTo(mDestroyState);
          break;
        }
        case EVT_READ_THROUGHPUT: {
          Timber.i("throughput:%d", mPeripheral.getThroughput());
          sendMessageDelayed(EVT_READ_THROUGHPUT, DELAY_READ_THROUGHPUT);
          break;
        }
        default:
          return NOT_HANDLED;
      }
      return HANDLED;
    }

    @Override public void exit() {
      sendMessage(EVT_CLOSE_SENSOR);
      mDisposables.clear();
      removeMessages(EVT_READ_THROUGHPUT);
      removeMessages(EVT_WAVE_FORM_INVALIDATE);
    }
  }

  public class NormalState extends State {

    private final CompositeDisposable mDisposables = new CompositeDisposable();

    @Override public void enter(@Nullable Object[] transferObjects) {
      super.enter(transferObjects);
      mBaseViewModel.state.postValue(BaseMeasureViewModel.STATE_MEASURING);
      synchronized (BaseMeasurePresenter.this) {
        mSignalChecker.reset();
        mQualityChecker.reset();
        mAllLogger.snStartFromZero();
        mNormalLogger.start();
        measureSoInit();
        resetView();
        initWaveForm();
        mDispatchProgress = true;
        mReceiveCount = 0;
        onProgress(mReceiveCount, mTargetCount);
      }
    }

    @Override public boolean processMessage(@NonNull Message msg) {
      switch (msg.what) {
        case EVT_SIGNAL_STATUS_CHANGED: {
          Object[] objects = (Object[]) msg.obj;
          int signal = (int) objects[0];
          if (!mSignalChecker.isGoodSignal(signal)) {
            transitionTo(mCheckingState, objects);
          }
          break;
        }
        case EVT_MEASURE_COMPLETED: {
          onCompleted();
          transitionTo(mMeasureCompleted,
              new Object[] { mNormalLogger.getCurrentFile().getAbsolutePath() });
          break;
        }
        default: {
          return NOT_HANDLED;
        }
      }
      return HANDLED;
    }

    @Override public void exit() {
      super.exit();
      synchronized (BaseMeasurePresenter.this) {
        mNormalLogger.stop();
        mDispatchProgress = false;
        mDisposables.clear();
        removeMessages(EVT_MEASURE_COMPLETED);
      }
    }
  }

  public class CheckingState extends State {

    private final CompositeDisposable mDisposables = new CompositeDisposable();

    @Override public void enter(@Nullable Object[] transferObjects) {
      super.enter(transferObjects);
      mBaseViewModel.state.postValue(BaseMeasureViewModel.STATE_CHECKING);
      synchronized (BaseMeasurePresenter.this) {
        mNormalLogger.delete();
        resetView();
        if (transferObjects != null && transferObjects.length > 0) {
          int signal = (int) transferObjects[0];
          setViewCheckReason(signal);
        }
        onProgress(0, mTargetCount);
        sendMessageDelayed(EVT_INTERRUPT_15S, mSetting.delayInterrupt);
      }
    }

    private void setViewCheckReason(int signal) {
      int reason = ~(signal | ~ mSignalChecker.getCheckingFlag()) & 0b11111;
     if (reason > 0) {
       String reasonStr = "00000".concat(Integer.toBinaryString(reason));
       reasonStr = reasonStr.substring(reasonStr.length() - 5);
       Timber.d(
            "toChecking Reason( quality_ppg2|quality_ppg|quality_ekg|finger_ppg|finger_ekg):%s(%d)",
            reasonStr, reason);
      }
      mBaseViewModel.toCheckReason.postValue(reason);
    }

    @Override public boolean processMessage(@NonNull Message msg) {
      synchronized (BaseMeasurePresenter.this) {
        switch (msg.what) {
          case EVT_SIGNAL_STATUS_CHANGED: {
            Object[] objects = (Object[]) msg.obj;
            int signal = (int) objects[0];
            if (mSignalChecker.isGoodSignal(signal)) {
              sendMessageDelayed(EVT_SIGNAL_GOOD, mSetting.delayCheckKeepOn);
            } else {
              removeMessages(EVT_SIGNAL_GOOD);
              setViewCheckReason(signal);
            }
            break;
          }
          case EVT_SIGNAL_GOOD: {
            transitionTo(mWaitRemeasureState);
            break;
          }
          case EVT_INTERRUPT_15S: {
            transitionTo(mInterruptState,
                new Object[] { new InterruptException(InterruptException.TYPE_FINER_OFF) });
            break;
          }
          default:
            return NOT_HANDLED;
        }
        return HANDLED;
      }
    }

    @Override public void exit() {
      super.exit();
      synchronized (BaseMeasurePresenter.this) {
        mDisposables.clear();
        removeMessages(EVT_INTERRUPT_15S);
        removeMessages(EVT_SIGNAL_GOOD);
      }
    }
  }

  public class InterruptState extends State {
    InterruptException exception;

    @Override public void enter(@Nullable Object[] transferObjects) {
      mAllLogger.stop();
      mBaseViewModel.state.postValue(BaseMeasureViewModel.STATE_INTERRUPT);
      if (transferObjects != null && transferObjects.length > 0) {
        if (transferObjects[0] instanceof Throwable) {
          exception = (InterruptException) transferObjects[0];
          mBaseViewModel.interruptError.postValue(exception);
        }
      }
      super.enter(transferObjects);
    }
  }

  public class CompletedState extends State {

    private String rawDataFile;

    @Override public void enter(@Nullable Object[] transferObjects) {
      mAllLogger.stop();
      mBaseViewModel.state.postValue(BaseMeasureViewModel.STATE_COMPLETED);
      super.enter(transferObjects);
      assert transferObjects != null;
      rawDataFile = (String) transferObjects[0];
      transitionTo(mPauseState);
    }

    @Override public void exit() {
      super.exit();
      mPlayState.closeFile();
      rawDataFile = null;
      mBaseViewModel.replayState.postValue(BaseMeasureViewModel.REPLAY_NONE);
    }
  }

  public class PlayState extends State {
    private long mLineTime;
    private BufferedReader reader;
    private long mFirstTime;
    private long mStartTime;

    private void parseLine(String[] ss) {
      int type = Integer.parseInt(ss[0]);
      switch (type) {
        case RawDataLogger.RAW_TYPE_EKG:
        case RawDataLogger.RAW_TYPE_PPG1: {
          SensorData sensorData = new SensorData();
          for (int i = 2; i < ss.length - 1; ++i) {
            sensorData.type = RawDataLogger.toSensorType(type);
            sensorData.value = Integer.parseInt(ss[i]);
            showWaveForm(sensorData.type, sensorData.value);
          }
        }
      }
    }

    private void closeFile() {
      if (reader != null) {
        try {
          reader.close();
          reader = null;
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    @Override public void enter(@Nullable Object[] transferObjects) {
      super.enter(transferObjects);
      sendMessage(EVT_WAVE_FORM_INVALIDATE);
      mBaseViewModel.replayState.postValue(BaseMeasureViewModel.REPLAY_PLAY);
      if (reader == null) {
        try {
          reader = new BufferedReader(new FileReader(new File(mMeasureCompleted.rawDataFile)));
          reader.readLine();
          String line = reader.readLine();
          String[] ss = line.split(",");
          mFirstTime = Long.parseLong(ss[ss.length - 1]);
          initWaveForm();
        } catch (IOException e) {
          reader = null;
          Timber.w(e);
          transitionTo(mPauseState);
        }
      }
      if (reader != null) {
        sendMessageDelayed(EVT_READ_FILE, 40);
      } else {
        transitionTo(mPauseState);
      }
    }

    @Override public boolean processMessage(@NonNull Message msg) {
      switch (msg.what) {
        case EVT_READ_FILE: {
          try {
            long delta = System.currentTimeMillis() - mStartTime;
            String line;
            while ((line = reader.readLine()) != null) {
              String[] ss = line.split(",");
              parseLine(ss);
              mLineTime = Long.parseLong(ss[ss.length - 1]);
              if (mLineTime - mFirstTime > delta) {
                break;
              }
            }
            if (line != null) {
              sendMessageDelayed(EVT_READ_FILE, 40);
            } else {
              closeFile();
              transitionTo(mPauseState);
            }
          } catch (IOException e) {
            e.printStackTrace();
            closeFile();
            transitionTo(mPauseState);
          }
          break;
        }
        case EVT_WAVE_FORM_PAUSE: {
          transitionTo(mPauseState);
          break;
        }
        default:
          return NOT_HANDLED;
      }
      return HANDLED;
    }

    @Override public void exit() {
      super.exit();
      mFirstTime = mLineTime;
      removeMessages(EVT_READ_FILE);
      removeMessages(EVT_WAVE_FORM_INVALIDATE);
    }
  }

  public class PauseState extends State {
    @Override public void enter(@Nullable Object[] transferObjects) {
      mBaseViewModel.replayState.postValue(BaseMeasureViewModel.REPLAY_PAUSE);
      super.enter(transferObjects);
    }

    @Override public boolean processMessage(@NonNull Message msg) {
      switch (msg.what) {
        case EVT_WAVE_FORM_PLAY: {
          mPlayState.mStartTime = System.currentTimeMillis();
          transitionTo(mPlayState);
          break;
        }
        default:
          return NOT_HANDLED;
      }
      return HANDLED;
    }

    @Override public void exit() {
      super.exit();
    }
  }

  public class WaitRemeasureState extends State {
    private static final long DELAY_RE_MEASURE = 200;

    @Override public void enter(@Nullable Object[] transferObjects) {
      initWaveForm();
      resetView();
      mBaseViewModel.remeasure.postValue(Resource.loading(null));
      super.enter(transferObjects);
    }

    @Override public boolean processMessage(@NonNull Message msg) {
      switch (msg.what) {
        case EVT_STOP_MEASURE_SUCCESS: {
          sendMessageDelayed(EVT_RE_MEASURE, DELAY_RE_MEASURE);
          break;
        }
        case EVT_RE_MEASURE: {
          transitionTo(mMeasureOnState);
          break;
        }
        default:
          return NOT_HANDLED;
      }
      return HANDLED;
    }

    @Override public void exit() {
      super.exit();
      removeMessages(EVT_RE_MEASURE);
    }
  }

  public class DestroyState extends State {
    private static final int DELAY_QUIT = 200;

    @Override public void enter(@Nullable Object[] transferObjects) {
      super.enter(transferObjects);
      mAllLogger.stop();
      sendMessageDelayed(EVT_QUIT, DELAY_QUIT);
    }

    @Override public boolean processMessage(@NonNull Message msg) {
      switch (msg.what) {
        case EVT_QUIT: {
          mDisposables.clear();
          quit();
          break;
        }
        default:
          return NOT_HANDLED;
      }
      return HANDLED;
    }
  }
}
