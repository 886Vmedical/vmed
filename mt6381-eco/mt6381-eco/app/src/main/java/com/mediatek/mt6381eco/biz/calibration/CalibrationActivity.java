package com.mediatek.mt6381eco.biz.calibration;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.mediatek.mt6381eco.R;

public class CalibrationActivity extends AppCompatActivity {
  private static final String FRAGMENT_TAG = "calibration";

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    Log.d("CalibrationActivity","onCreate()");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.layout_container);

    Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
    if (fragment == null) {
      fragment = new CalibrationFragment();
      getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment, FRAGMENT_TAG).commit();
    }
  }
}
