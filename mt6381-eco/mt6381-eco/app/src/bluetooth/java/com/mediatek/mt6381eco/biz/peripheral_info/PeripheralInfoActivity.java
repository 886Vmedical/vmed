package com.mediatek.mt6381eco.biz.peripheral_info;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.mediatek.blenativewrapper.StateInfo;
import com.mediatek.mt6381.ble.MT6381Peripheral;
import com.mediatek.mt6381.ble.MT6381SystemInfoParser;
import com.mediatek.mt6381eco.R;
import com.mediatek.mt6381eco.biz.peripheral.IPeripheral;
import com.mediatek.mt6381eco.biz.peripheral.PeripheralService;
import com.mediatek.mt6381eco.ui.OnBackPressedListener;

import javax.inject.Inject;

public class PeripheralInfoActivity extends AppCompatActivity {
  private static final String FRAGMENT_TAG = "FRAGMENT";

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d("PeripheralInfoActivity","onCreate");
    setContentView(R.layout.layout_container);
    FragmentManager fm = getSupportFragmentManager();
    PeripheralInfoFragment fragment = (PeripheralInfoFragment) fm.findFragmentByTag(FRAGMENT_TAG);
    if (fragment == null) {
      fragment = new PeripheralInfoFragment();
      fm.beginTransaction().replace(R.id.container, fragment, FRAGMENT_TAG).commit();
    }
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home: {
        finish();
        break;
      }
    }
    return super.onOptionsItemSelected(item);
  }

  @Override public void onBackPressed() {
    for(Fragment fragment:getSupportFragmentManager().getFragments()){
      if(fragment instanceof OnBackPressedListener){
        ((OnBackPressedListener)fragment).onBackPressed();
        return;
      }
    }
    super.onBackPressed();
  }

}
