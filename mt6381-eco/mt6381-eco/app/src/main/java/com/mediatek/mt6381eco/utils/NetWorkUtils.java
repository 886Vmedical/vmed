package com.mediatek.mt6381eco.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter public class NetWorkUtils {
  private static NetWorkUtils instance;
  private Context mContext;
  private boolean isNetworkConnected;

  public static synchronized NetWorkUtils getInstance() {
    if (instance == null) instance = new NetWorkUtils();
    return instance;
  }

  public boolean checkNetworkConnected(Context mContext) {
    boolean pass = false;
    if (mContext != null) {
      ConnectivityManager mConnectivityManager =
          (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
      if (mNetworkInfo != null) {
        pass = mNetworkInfo.isAvailable();
      }
    }
    return pass;
  }
}
