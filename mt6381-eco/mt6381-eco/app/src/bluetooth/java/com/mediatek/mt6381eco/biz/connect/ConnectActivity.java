package com.mediatek.mt6381eco.biz.connect;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import com.mediatek.mt6381eco.R;

public class ConnectActivity extends AppCompatActivity {
  private static final String TAG_FRAGMENT = "CONNECT";

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.layout_container);
    Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT);
    if (fragment == null) {
      fragment = new ConnectFragment();
      getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment, TAG_FRAGMENT).commit();
    }
  }
}
