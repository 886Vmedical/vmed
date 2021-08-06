package com.mediatek.mt6381eco;

import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.stetho.Stetho;
import com.mediatek.mt6381eco.biz.startup.StartupActivity;
import com.mediatek.mt6381eco.biz.viewmodel.AppViewModel;
import com.mediatek.mt6381eco.dagger.DaggerAppComponent;
import com.mediatek.mt6381eco.log.MFileLogger;
import com.mediatek.mt6381eco.log.TimberFileLogger;
import com.mediatek.mt6381eco.ui.ContextUtils;
import com.mediatek.mt6381eco.ui.interfaces.GuestPage;
//import com.squareup.leakcanary.LeakCanary;
//import com.squareup.leakcanary.RefWatcher;

import java.io.File;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.support.DaggerApplication;
import timber.log.Timber;

public class MApplication extends DaggerApplication {

  //private static RefWatcher refWatcher;
  @Inject AppViewModel mAppViewModel;

/*  public static RefWatcher getRefWatcher() {
    return refWatcher;
  }*/

  @Override public void onCreate() {
    super.onCreate();
    /*if (LeakCanary.isInAnalyzerProcess(this)) {
      // This process is dedicated to LeakCanary for heap analysis.
      // You should not init your app in this process.
      return;
    }*/
    // Set Log File
    File logFile = new File(
        String.format("%s/mtklog/%s/", Environment.getExternalStorageDirectory().getAbsolutePath(),
            BuildConfig.APPLICATION_ID));
    MFileLogger.setDefaultFolder(logFile);
    Timber.plant(new TimberFileLogger());
    //refWatcher = LeakCanary.install(this);
    if (BuildConfig.DEBUG) {
      Stetho.initializeWithDefaults(this);
    }

    ContextUtils.init(this);
    observeActivity();


  }

  @Override protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
    return DaggerAppComponent.builder().create(this);
  }

  private void observeActivity() {

    registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
      @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        Timber.i("onActivityCreated:%s", activity.getClass().getSimpleName());
        if (activity instanceof LifecycleOwner) {
          if (activity instanceof GuestPage) {
            return;
          }
          mAppViewModel.needRelogin.observe((LifecycleOwner) activity, needLogin -> {
            if (needLogin != null && needLogin) {
              for (Fragment fragment : ((AppCompatActivity) activity).getSupportFragmentManager()
                  .getFragments()) {
                if (fragment instanceof GuestPage) {
                  return;
                }
              }
              //delete by herman for temp
              /*new MaterialDialog.Builder(activity).content(R.string.token_invalid_need_re_login)
                  .positiveText(R.string.relogin)
                  .onPositive((dialog, which) -> {
                    Intent intent = new Intent(activity, StartupActivity.class);
                    intent.addFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    activity.finishAffinity();
                    activity.startActivity(intent);
                  })
                  .negativeText(R.string.no)
                  .show();*/
            }
          });
        }
      }

      @Override public void onActivityStarted(Activity activity) {
        Timber.i("onActivityStarted:%s", activity.getClass().getSimpleName());
      }

      @Override public void onActivityResumed(Activity activity) {
        Timber.i("onActivityResumed:%s", activity.getClass().getSimpleName());
      }

      @Override public void onActivityPaused(Activity activity) {
        Timber.i("onActivityPaused:%s", activity.getClass().getSimpleName());
      }

      @Override public void onActivityStopped(Activity activity) {
        Timber.i("onActivityStopped:%s", activity.getClass().getSimpleName());
      }

      @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        Timber.i("onActivitySaveInstanceState:%s", activity.getClass().getSimpleName());
      }

      @Override public void onActivityDestroyed(Activity activity) {
        Timber.i("onActivityDestroyed:%s", activity.getClass().getSimpleName());
      }
    });
  }
}
