package com.mediatek.mt6381eco.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import com.mediatek.mt6381eco.biz.peripheral.IPeripheral;
import com.mediatek.mt6381eco.biz.peripheral.PeripheralService;

public class BasePeripheralActivity extends BaseActivity implements ServiceConnection {
  private boolean mBond = false;
  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    bindService(new Intent(this, PeripheralService.class), this, Context.BIND_AUTO_CREATE);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    if(mBond) {
      unbindService(this);
    }
  }

  protected void attach(IPeripheral peripheral){

  }

  @Override public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
    mBond = true;
    attach((IPeripheral) iBinder);
  }



  @Override public void onServiceDisconnected(ComponentName componentName) {
    mBond = false;
  }
}
