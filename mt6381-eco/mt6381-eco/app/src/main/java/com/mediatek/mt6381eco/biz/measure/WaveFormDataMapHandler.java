package com.mediatek.mt6381eco.biz.measure;

import android.util.SparseArray;
import com.mediatek.mt6381eco.biz.peripheral.SensorData;
import java.util.ArrayList;

class WaveFormDataMapHandler {
  private final SparseArray<ArrayList<Float>> mViewModel;
  private final SparseArray<WaveFormDataHandler> mHandlers = new SparseArray<>();


  public WaveFormDataMapHandler(SparseArray<ArrayList<Float>> viewModel){
    mViewModel = viewModel;
    reset();
  }
  public synchronized void reset() {
    mHandlers.clear();
    for(int i = 0;i < mViewModel.size(); ++i){
      int key = mViewModel.keyAt(i);
      ArrayList<Float> item = mViewModel.get(key);
      synchronized (item){
        item.clear();
      }
      mHandlers.put(key, createHandler(key, item));
    }
  }


  private WaveFormDataHandler createHandler(int type, ArrayList<Float> data){
    switch (type){
      case SensorData.DATA_TYPE_EKG:{
        return new EKGWaveFormDataHandler(data);
      }
      case SensorData.DATA_TYPE_PPG1:{
        return new PPGWaveFormDataHandler(data);
      }
    }
    return null;
  }

  public synchronized void receiveData(int type, int value){
    WaveFormDataHandler handler = mHandlers.get(type);
    if(handler != null){
      handler.receiveData(value);
    }
  }
  public void invalidate(){
    for(int i =0;i< mHandlers.size(); ++i){
      mHandlers.valueAt(i).invalidate();
    }
  }


}