package com.mediatek.mt6381eco.utils;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import com.mediatek.mt6381eco.ui.BaseFragment;

public class ServiceBinding {
  private final OnBonding mCallback;
  private final Context mContext;
  private final Class<? extends Service> mClsService;
  public boolean mBonded = false;
  private final ServiceConnection mServiceConnection = new ServiceConnection() {

    @Override public void onServiceConnected(ComponentName name, IBinder service) {
      mBonded = true;
      mCallback.onServiceConnected(service);
    }

    @Override public void onServiceDisconnected(ComponentName name) {
      mBonded = false;
    }
  };

  private ServiceBinding(Context context, Class<? extends Service> clsService, OnBonding callback) {
    mCallback = callback;
    mContext = context;
    mClsService = clsService;
  }

  public static Unbind bindService(BaseFragment baseFragment, Class<? extends Service> clsService,
      OnBonding callback) {
    return new ServiceBinding(baseFragment.getActivity().getApplicationContext(), clsService,
        callback).bind();
  }

  private Unbind bind() {
    mContext.bindService(new Intent(mContext, mClsService), mServiceConnection,
        Context.BIND_AUTO_CREATE);
    return new Unbind() {
      @Override public void unbind() {
        if (isBonded()) {
          mContext.unbindService(mServiceConnection);
        }
      }

      @Override public boolean isBonded() {
        return mBonded;
      }
    };
  }


  public interface OnBonding {
    void onServiceConnected(IBinder service);
  }

  public interface Unbind {
    void unbind();
    boolean isBonded();
  }
}
