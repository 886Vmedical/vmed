package com.mediatek.mt6381eco.biz.measure;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import com.mediatek.mt6381eco.R;

public class MeasureActivity extends AppCompatActivity {
  private static final String FRAGMENT_TAG = "measure";
  public static MeasureActivity mActivity;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mActivity =this;
    setContentView(R.layout.layout_container);

    Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
    if (fragment == null) {
      fragment = new MeasureFragment();
      getSupportFragmentManager().beginTransaction()
          .replace(R.id.container, fragment, FRAGMENT_TAG)
          .commit();
    }
  }
}
