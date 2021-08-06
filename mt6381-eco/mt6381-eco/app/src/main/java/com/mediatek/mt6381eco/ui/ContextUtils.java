package com.mediatek.mt6381eco.ui;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.ConnectivityManager;
import com.mediatek.blenativewrapper.exceptions.CommunicateException;
import com.mediatek.blenativewrapper.exceptions.ConnectException;
import com.mediatek.blenativewrapper.exceptions.DisconnectException;
import com.mediatek.blenativewrapper.exceptions.ScanException;
import com.mediatek.mt6381eco.R;
import com.mediatek.mt6381eco.exceptions.ConnectionLostException;
import com.mediatek.mt6381eco.exceptions.ThroughputException;
import com.mediatek.mt6381eco.network.RetrofitException;
import com.mediatek.mt6381eco.ui.exceptions.ActivityIntentActionException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeoutException;

public class ContextUtils {
  private static Context sContext = null;

  public static void init(Context application) {
    sContext = application;
  }

  public static boolean isNetworkConnected() {
    ConnectivityManager cm =
        (ConnectivityManager) sContext.getSystemService(Context.CONNECTIVITY_SERVICE);
    return cm.getActiveNetworkInfo() != null;
  }

  public static String getErrorMessage(Throwable throwable) {
    if (throwable instanceof RetrofitException) {
      RetrofitException retrofitException = (RetrofitException) throwable;
      switch (retrofitException.getKind()) {
        case UNEXPECTED: {
          return sContext.getString(R.string.error_network_unexpected, throwable.getMessage());
        }
        case NETWORK: {
          if (!isNetworkConnected()) {
            return sContext.getString(R.string.error_network_unavailable);
          } else {
            return sContext.getString(R.string.error_network_maybe_unavailable);
          }
        }
        case HTTP: {
          return retrofitException.getMessage();
        }
      }
    } else if (throwable instanceof ActivityIntentActionException) {
      if (((ActivityIntentActionException) throwable).getIntent()
          .getAction()
          .equals(BluetoothAdapter.ACTION_REQUEST_ENABLE)) {
        return sContext.getString(R.string.error_fail_to_enable_bluetooth);
      }
    } else if (throwable instanceof ScanException) {
      return sContext.getString(R.string.error_scan_fail, throwable.getMessage());
    } else if (throwable instanceof CommunicateException) {
      return sContext.getString(R.string.error_peripheral_communicate, throwable.getMessage());
    } else if (throwable instanceof ConnectException) {
      return sContext.getString(R.string.error_peripheral_connect, throwable.getMessage());
    } else if (throwable instanceof DisconnectException) {
      return sContext.getString(R.string.error_peripheral_disconnect, throwable.getMessage());
    } else if (throwable instanceof ConnectionLostException) {
      return sContext.getString(R.string.error_peripheral_connection_lost);
    } else if (throwable instanceof ThroughputException) {
      return sContext.getString(R.string.error_throughput, throwable.getCause().getMessage());
    }else if(throwable instanceof TimeoutException){
      return sContext.getString(R.string.timeout);
    }
    return throwable.getMessage();
  }

  public static InputStream getAssetInputStream(String fileName) throws IOException {
    return sContext.getAssets().open(fileName);
  }
}
