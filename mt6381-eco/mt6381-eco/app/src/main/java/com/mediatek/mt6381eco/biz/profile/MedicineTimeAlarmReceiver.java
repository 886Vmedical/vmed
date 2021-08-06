package com.mediatek.mt6381eco.biz.profile;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.support.v4.app.NotificationCompat;
import android.util.Log;

import android.widget.Toast;

import com.mediatek.mt6381eco.R;

public class MedicineTimeAlarmReceiver extends BroadcastReceiver {

  private NotificationManager m_notificationMgr = null;
  private static final int NOTIFICATION_FLAG = 1;

  @Override
  public void onReceive(Context context, Intent intent) {
      m_notificationMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
      String nTime = intent.getStringExtra("medTime");
      String fullTime = nTime +":00 ~"  +nTime  + ":59 ";
      Log.d("Time Count", "nTime: "+nTime);
      PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
      Notification notify = new Notification.Builder(context)
              .setSmallIcon(R.drawable.icon_profile_medicine)
              .setTicker(context.getString(R.string.medstickTitle))
              .setContentTitle(context.getString(R.string.medtimeTitle))
              .setContentText(context.getString(R.string.medtimeContent)+ fullTime + context.getString(R.string.medtimeContentExtra))
              .setContentIntent(pendingIntent)
              .setNumber(1)
              .setStyle(new Notification.BigTextStyle().bigText(context.getString(R.string.medtimeContent)+ fullTime + context.getString(R.string.medtimeContentExtra)))
              .getNotification();
      notify.defaults |= Notification.DEFAULT_VIBRATE;
      notify.defaults |= Notification.DEFAULT_SOUND;
      notify.flags |= Notification.FLAG_AUTO_CANCEL;
      m_notificationMgr.notify(NOTIFICATION_FLAG, notify);
  }

}
