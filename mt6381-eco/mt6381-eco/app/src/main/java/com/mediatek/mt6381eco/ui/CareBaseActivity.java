package com.mediatek.mt6381eco.ui;

import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import com.mediatek.mt6381eco.ui.BaseActivity;

public abstract class CareBaseActivity extends BaseActivity implements LifecycleRegistryOwner {
  private final LifecycleRegistry mRegistry = new LifecycleRegistry(this);

  @Override public LifecycleRegistry getLifecycle() {
    return mRegistry;
  }
}
