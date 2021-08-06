package com.mediatek.mt6381eco.dagger;

import android.app.Application;
import android.os.Environment;
import com.mediatek.mt6381eco.BuildConfig;
import java.io.File;
import javax.inject.Inject;

class AppContext implements IAppContext {

  private final Application mApplication;

  @Inject AppContext(Application application) {
    mApplication = application;
  }

  @Override public File getDataDir() {
    return new File(
        String.format("%s/mtklog/%s/", Environment.getExternalStorageDirectory().getAbsolutePath(),
            BuildConfig.APPLICATION_ID));
    // return ContextCompat.getDataDir(mApplication);
  }

  @Override public File getDownloadDir() {
    File downloadDir =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    return new File(downloadDir, BuildConfig.APPLICATION_ID);
  }
}
