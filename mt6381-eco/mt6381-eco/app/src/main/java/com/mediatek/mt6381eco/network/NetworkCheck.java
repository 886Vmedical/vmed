package com.mediatek.mt6381eco.network;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter public class NetworkCheck {
  public static String NETWORK_CONNECTED = "NETWORK_CONNECTED";
  private final Application application;

  public NetworkCheck(Application application) {
    this.application = application;
  }

  public boolean checkNetworkConnected() {
    boolean pass = false;
    ConnectivityManager mConnectivityManager =
        (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
    if (mNetworkInfo != null) {
      pass = mNetworkInfo.isAvailable();
    }
    return pass;
  }
}
