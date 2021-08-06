package com.mediatek.mt6381eco.biz.measure;

import android.arch.lifecycle.MutableLiveData;
import android.util.Log;
import android.util.SparseArray;
import com.mediatek.mt6381eco.biz.peripheral.SensorData;
import com.mediatek.mt6381eco.dagger.FragmentScoped;
import com.mediatek.mt6381eco.viewmodel.Resource;

import java.util.ArrayList;
import java.util.Random;

import javax.inject.Inject;

@FragmentScoped public class BaseMeasureViewModel {
  public static final int STATE_READY = 0;
  public static final int STATE_MEASURING = 1;
  public static final int STATE_CHECKING = 2;
  public static final int STATE_INTERRUPT = 3;
  public static final int STATE_COMPLETED = 4;

  public static final int REPLAY_NONE = 0;
  public static final int REPLAY_PAUSE = 1;
  public static final int REPLAY_PLAY = 2;

  public final MutableLiveData<String> nickname = new MutableLiveData<>();
  public final MutableLiveData<Integer> dataLostType = new MutableLiveData<>();
  public final MutableLiveData<Integer> state = new MutableLiveData<>();
  public final MutableLiveData<Integer> progress = new MutableLiveData<>();
  public final MutableLiveData<HRSpo2> hrspo2 = new MutableLiveData<>();
  public final MutableLiveData<BloodPressure> bloodPressure = new MutableLiveData<>();
  public final MutableLiveData<FatiguePressure> fatiguePressure = new MutableLiveData<>();
  public final MutableLiveData<Integer> toCheckReason = new MutableLiveData<>();
  public final MutableLiveData<Throwable> interruptError = new MutableLiveData<>();
  public final MutableLiveData<Resource> mPrepareLoading = new MutableLiveData<>();
  public final MutableLiveData<Integer> replayState = new MutableLiveData<>();
  public final MutableLiveData<Resource> remeasure = new MutableLiveData<>();
  public final MutableLiveData<Boolean> ekgChecking = new MutableLiveData<>();
  public final MutableLiveData<Boolean> ppg1Checking = new MutableLiveData<>();
  public final MutableLiveData<Boolean> ppg2Checking = new MutableLiveData<>();

  public final SparseArray<ArrayList<Float>> waveData = new SparseArray<>();
  private int mLastProgress = -1;
  private HRSpo2 mBufferHRSpo2 = new HRSpo2();

  @Inject BaseMeasureViewModel() {
    state.setValue(STATE_READY);
    setProgress(mLastProgress);
    waveData.put(SensorData.DATA_TYPE_EKG, new ArrayList<>());
    waveData.put(SensorData.DATA_TYPE_PPG1, new ArrayList<>());
    replayState.setValue(REPLAY_NONE);
  }

  //for performance
  public void setProgress(int value) {
    if (mLastProgress != value) {
      progress.postValue(value);
    }
    mLastProgress = value;
  }

  //for performance
  public void setHrSpo2(int hr, int spo2) {
    if (hr != mBufferHRSpo2.heartRate || spo2 != mBufferHRSpo2.spo2) {
      mBufferHRSpo2 = new HRSpo2();
      //if(0 == hr){
      //  Random rd = new Random();
      //  mBufferHRSpo2.heartRate = rd.nextInt(80-70) + 70;
      //}else {
      mBufferHRSpo2.heartRate = hr;
      //}
      mBufferHRSpo2.spo2 = spo2;
      hrspo2.postValue(mBufferHRSpo2);
    }
  }

  public static class HRSpo2 {
    public int heartRate = -1;
    public int spo2 = -1;
  }

  public static class BloodPressure {
    public int dbp = -1;
    public int sbp = -1;

    public BloodPressure() {

    }

    public BloodPressure(int sbp, int dbp) {
      this.sbp = sbp;
      this.dbp = dbp;
      Log.d("BloodPressure","sbp: " + sbp);
      Log.d("BloodPressure","dbp: " + dbp);
    }
  }

  public static class FatiguePressure {
    public int fatigue = -1;
    public int pressure = -1;

    public FatiguePressure() {

    }

    public FatiguePressure(int fatigue, int pressure) {
      this.fatigue = fatigue;
      this.pressure = pressure;
    }
  }
}
