package com.mediatek.mt6381eco.biz.flavor;

import android.app.Activity;
import android.content.Intent;
import com.mediatek.mt6381eco.biz.connect.ConnectActivity;
import com.mediatek.mt6381eco.db.EasyDao;
import com.mediatek.mt6381eco.db.entries.BondDevice;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton public class FlavorUtils implements IFlavorUtils {
  private final EasyDao mEasyDao;

  @Inject FlavorUtils(EasyDao easyDao) {
    mEasyDao = easyDao;
  }

  @Override public void onHomeStart(Activity activity) {
    BondDevice bondDevice = mEasyDao.find(BondDevice.class);
    if (bondDevice == null) {
      activity.startActivity(new Intent(activity, ConnectActivity.class));
    }
  }
}
