package com.mediatek.mt6381eco.dagger;

import java.io.File;

public interface IAppContext {
  File getDataDir();
  File getDownloadDir();
}
