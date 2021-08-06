package com.mediatek.mt6381.ble;

import com.mediatek.mt6381.ble.cache.MemoryBuffer;
import com.mediatek.mt6381eco.biz.peripheral.SensorData;
import java.util.ArrayList;
import timber.log.Timber;

public class SensorQueue {
  private static final int ROTATE = 256;
  private static final int MEMORY_BUFFER_SIZE = 500;
  private final MemoryBuffer<SensorData> mMemoryBuffer = new MemoryBuffer<>(MEMORY_BUFFER_SIZE);
  private final ArrayList<SensorData> mList = new ArrayList<>();
  private int mLastSN = -1;
  private int mLastRealSn = -1;

  public SensorQueue(){
    reset();
  }

  public void reset(){
    mList.clear();
    mMemoryBuffer.clear();
    mLastSN = -1 ;
    mLastRealSn = -1;
  }

  public void add(SensorData sensorData){
    SensorData bufferItem = mMemoryBuffer.fetch();
    if(bufferItem == null){
      bufferItem = new SensorData();
    }
    sensorData.copyTo(bufferItem);

    int delta = bufferItem.sn - mLastRealSn;
    mLastRealSn = bufferItem.sn;
    if(delta < 0){
      delta += ROTATE;
    }
    if(delta != 1){
      Timber.w("data_lost delta:%d : %d - %d" ,delta, bufferItem.type, bufferItem.sn);
    }
    bufferItem.sn = mLastSN + delta;
    mLastSN = bufferItem.sn;
    mList.add(bufferItem);
  }

  public SensorData poll(){
    SensorData releaseItem = mList.remove(0);
    mMemoryBuffer.release(releaseItem);
    return releaseItem;
  }

  public SensorData peek(){
    if(mList.size() <1){
      return null;
    }
    return mList.get(0);
  }

}
