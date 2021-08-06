package com.mediatek.mt6381eco.biz.peripheral_info;

import android.arch.lifecycle.MutableLiveData;
import com.mediatek.mt6381eco.dagger.FragmentScoped;
import com.mediatek.mt6381eco.viewmodel.Resource;
import java.util.Date;
import javax.inject.Inject;

@FragmentScoped
public class PeripheralInfoViewModel {
  public final MutableLiveData<String> deviceName = new MutableLiveData<>();
  public final MutableLiveData<Resource<String>> newFirmware = new MutableLiveData<>();
  public final MutableLiveData<Resource<Float>> downloadProgress = new MutableLiveData<>();
  public final MutableLiveData<Resource<Float>> fotaProgress = new MutableLiveData<>();
  public final MutableLiveData<Resource<PeripheralInfo>> info = new MutableLiveData<>();

  @Inject PeripheralInfoViewModel() {

  }

  public static class PeripheralInfo {
    public int power;
    public Date synced;
    //todo by herman
    public String version;
  }

}
