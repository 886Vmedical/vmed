package com.mediatek.mt6381eco.dagger;

import javax.inject.Inject;

class SupportSensorTypesImpl extends SupportSensorTypes{
  @Inject SupportSensorTypesImpl(){
    
  }
  @Override public int[] getTypeIntArray() {
    return DATA_TYPE_FULL;
  }
}
