package com.mediatek.blenativewrapper.utils;

import android.util.Log;
import java.lang.reflect.Method;

public class PlatformUtils {
  private static final String TAG = "PlatformUtils";
  private static String platform = "NA";

  static {
    Method getMethod;
    try {
      getMethod = Class.forName("android.os.SystemProperties")
          .getMethod("get", String.class);
      String key = "ro.mediatek.platform";
      platform = ((String) getMethod.invoke(null, key));
      Log.d(TAG, "SystemProperties platform=" + platform);
    } catch (Exception e) {
      Log.e(TAG, "reflect SystemProperties fail: " + e.toString());
    }
  }


  public static boolean isMTK(){
    return platform != null && platform.toUpperCase().contains("MT");
  }
  public static String getPlatform(){
    return platform;
  }
}