package com.mediatek.mt6381eco.biz.profile;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import com.mediatek.mt6381eco.dagger.ActivityScoped;
import java.util.Date;
import javax.inject.Inject;
import lombok.Getter;

@ActivityScoped @Getter public class ProfileViewModel {
  public static final int PERSONAL_STATUS_NONE = 0;
  public static final int PERSONAL_STATUS_HYPERTENSION = 1;
  public static final int PERSONAL_STATUS_HYPOTENSION = 2;
  private final MutableLiveData<String> profileId = new MutableLiveData<>();
  private final MutableLiveData<String> nickName = new MutableLiveData<>();
  private final MutableLiveData<Integer> gender = new MutableLiveData<>();
  private final MutableLiveData<Date> birthday = new MutableLiveData<>();
  private final MutableLiveData<Integer> height = new MutableLiveData<>();
  private final MutableLiveData<ValueUnit> weight = new MutableLiveData<>();
  private final MutableLiveData<Integer> personalStatus = new MutableLiveData<>();
  private final MutableLiveData<Integer> takeMedicineTime = new MutableLiveData<>();
  public final MutableLiveData<Boolean> isCalibrated = new MutableLiveData<>();

  @Inject public ProfileViewModel() {
    gender.setValue(-1);
    isCalibrated.setValue(false);
  }
}
