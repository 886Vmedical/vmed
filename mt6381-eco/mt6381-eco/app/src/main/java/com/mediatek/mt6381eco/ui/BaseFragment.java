package com.mediatek.mt6381eco.ui;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mediatek.mt6381eco.MApplication;
import com.mediatek.mt6381eco.R;
import com.mediatek.mt6381eco.dagger.Injectable;
import com.mediatek.mt6381eco.mvp.BaseView;
import dagger.android.support.AndroidSupportInjection;

public abstract class BaseFragment extends Fragment implements LifecycleRegistryOwner, BaseView {

  private final LifecycleRegistry mLifecycleRegistry = new LifecycleRegistry(this);
  private MaterialDialog mProgressDialog;
  private boolean mInjected = false;
  private Unbinder mViewUnBinder;
  private Throwable mLastError = null;

  public BaseFragment() {
    setRetainInstance(true);
  }

  protected void applyActionBar(ActionBar actionBar) {

  }

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    FragmentActivity activity = getActivity();
    if (activity instanceof AppCompatActivity) {
      ActionBar actionBar = ((AppCompatActivity) activity).getSupportActionBar();
      if (actionBar != null) {
        applyActionBar(actionBar);
      }
    }
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (this instanceof Injectable && !mInjected) {
      AndroidSupportInjection.inject(this);
      mInjected = true;
    }
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    getLifecycle().handleLifecycleEvent(Lifecycle.Event.ON_CREATE);
    mViewUnBinder = ButterKnife.bind(this, view);
    initView();
    super.onViewCreated(view, savedInstanceState);
    initView(savedInstanceState);
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
  }

  @Override public void onStart() {
    super.onStart();
    getLifecycle().handleLifecycleEvent(Lifecycle.Event.ON_START);
  }

  @Override public void onResume() {
    super.onResume();
    getLifecycle().handleLifecycleEvent(Lifecycle.Event.ON_RESUME);
  }

  @Override public void onPause() {
    super.onPause();
    getLifecycle().handleLifecycleEvent(Lifecycle.Event.ON_PAUSE);
  }

  @Override public void onStop() {
    super.onStop();
    getLifecycle().handleLifecycleEvent(Lifecycle.Event.ON_STOP);
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    getLifecycle().handleLifecycleEvent(Lifecycle.Event.ON_DESTROY);
    stopLoading();
    mViewUnBinder.unbind();
    mViewUnBinder = null;
    mProgressDialog = null;
  }


  @Override public void onDestroy() {
    super.onDestroy();
    getLifecycle().handleLifecycleEvent(Lifecycle.Event.ON_DESTROY);
    //delete by herman for leaks
    //MApplication.getRefWatcher().watch(this);
  }

  //before restore
  protected void initView(){

  }
  //after restore
  protected void initView(Bundle savedInstanceState){

  }

  @Override public LifecycleRegistry getLifecycle() {
    return mLifecycleRegistry;
  }

  @Override public void startLoading(Object... args) {
    if (args.length > 0 && args[0] instanceof String) {
      startLoading((String) args[0]);
    }else {
      startLoading("");
    }
  }

  @Override public void stopLoading() {
    if (mProgressDialog != null && mProgressDialog.isShowing()) {
      mProgressDialog.dismiss();
    }
    mProgressDialog = null;
  }

  @Override public void showError(Throwable throwable) {
    if(mLastError != throwable) {
      Toast.makeText(getActivity().getApplicationContext(), ContextUtils.getErrorMessage(throwable),
          Toast.LENGTH_LONG).show();
    }
    mLastError = throwable;
  }

  protected void startLoading(String message) {
    mProgressDialog = new MaterialDialog.Builder(getActivity())
        .content(message)
        .progress(true, 0)
        .cancelable(false)
        .show();
  }


  public void onAttachedToWindow() {
  }
}
