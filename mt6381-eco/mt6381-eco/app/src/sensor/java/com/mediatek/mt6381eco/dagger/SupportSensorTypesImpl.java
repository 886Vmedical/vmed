package com.mediatek.mt6381eco.dagger;

import android.app.Application;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import com.mediatek.mt6381eco.biz.peripheral.SensorData;
import com.mediatek.mt6381eco.utils.DataUtils;
import java.util.List;
import javax.inject.Inject;

public class SupportSensorTypesImpl extends SupportSensorTypes {

  private final int[] mSupportTypes;

  @Inject SupportSensorTypesImpl(Application application){
    mSupportTypes = loadSupportTypes(application);
  }

  private int[] loadSupportTypes(Application application){
    SensorManager sensorManager =
        (SensorManager) application.getSystemService(Context.SENSOR_SERVICE);
    List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);
    int[] tmpTypes = new int[DATA_TYPE_FULL.length];
    int index = 0;
    for(Sensor sensor:sensorList){
      switch (sensor.getName()){
        case STRING_TYPE_EKG:{
          tmpTypes[index++] = SensorData.DATA_TYPE_EKG;
          break;
        }
        case STRING_TYPE_PPG1:{
          tmpTypes[index++] = SensorData.DATA_TYPE_PPG1;
          break;
        }
        case STRING_TYPE_PPG2:{
          tmpTypes[index++] = SensorData.DATA_TYPE_PPG2;
          break;
        }
      }
    }
    return  DataUtils.and(DATA_TYPE_FULL, tmpTypes);
  }

  @Override public int[] getTypeIntArray() {
    return mSupportTypes;
  }
}
