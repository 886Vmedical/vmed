package com.mediatek.mt6381eco.log;

import android.os.Build;
import com.mediatek.mt6381eco.BuildConfig;
import com.mediatek.mt6381eco.utils.MTimeUtils;
import timber.log.Timber;

public class TimberFileLogger extends Timber.DebugTree {
  private final MFileLogger mFileLogger = new MFileLogger("'APK_LOG_'yyyy_MM_dd_HH_mm_ss.SSS.'txt'");
  private long mTomorrow = 0L;

  @Override protected void log(int priority, String tag, String message, Throwable t) {
    super.log(priority, tag, message, t);
    if (System.currentTimeMillis() > mTomorrow) {
      mTomorrow = MTimeUtils.clearTime(System.currentTimeMillis() + 1000 * 3600 * 24);
      mFileLogger.reset();
      /*delete by herman for GITSHA
	  mFileLogger.write("%s/(%d)/%s/%s/%s/%s", Build.MODEL, Build.VERSION.SDK_INT,
          BuildConfig.APPLICATION_ID, BuildConfig.VERSION_NAME, BuildConfig.BUILD_TYPE,
          BuildConfig.GIT_SHA);*/
      mFileLogger.newLine();
    }
    mFileLogger.write(MTimeUtils.formatTimeMillSeconds(System.currentTimeMillis()));
    mFileLogger.write(' ');
    mFileLogger.write(String.valueOf(priority));
    mFileLogger.write('/');
    mFileLogger.write(tag);
    mFileLogger.write(':');
    mFileLogger.write(message);
    mFileLogger.newLine();
  }
}
