package com.mediatek.mt6381eco.ui;

import android.app.Activity;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import com.mediatek.mt6381eco.R;
import java.util.List;
import timber.log.Timber;

public class ContainerActivity extends AppCompatActivity implements LifecycleRegistryOwner {
  public static final String CLS_NAME_FRAGMENT = "cls_name_fragment";
  public static final String ACTIVITY_THEME = "theme";
  private static final String TAG_FRAGMENT = "biz_fragment";
  private final LifecycleRegistry mRegistry = new LifecycleRegistry(this);

  public static Intent makeIntent(Activity context, Class<? extends Fragment> cls) {
    return makeIntent(context, cls, -1);
  }

  public static Intent makeIntent(Activity context, Class<? extends Fragment> cls, int themeStyle) {
    return new Intent(context, ContainerActivity.class).putExtra(CLS_NAME_FRAGMENT, cls.getName())
        .putExtra(ACTIVITY_THEME, themeStyle);
  }

  @Override public LifecycleRegistry getLifecycle() {
    return mRegistry;
  }

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    int themeStyle = getIntent().getIntExtra(ACTIVITY_THEME, -1);
    if (themeStyle > -1) {
      setTheme(themeStyle);
    }

    setContentView(R.layout.layout_container);
    Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT);
    if (fragment == null) {
      String clsFragment = getIntent().getStringExtra(CLS_NAME_FRAGMENT);
      try {
        fragment = (Fragment) Class.forName(clsFragment).newInstance();
        fragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.container, fragment, TAG_FRAGMENT)
            .commit();
      } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
        Timber.e(e);
      }
    }
  }

  @Override public void onAttachedToWindow() {
    super.onAttachedToWindow();
    List<Fragment> fragments = getSupportFragmentManager().getFragments();
    for (Fragment fragment : fragments) {
      if (fragment instanceof BaseFragment) {
        ((BaseFragment) fragment).onAttachedToWindow();
      }
    }
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    if(super.onOptionsItemSelected(item)){
      return true;
    }
    switch (item.getItemId()){
      case android.R.id.home:{
        finish();
        return true;
      }
    }
    return false;

  }
}
