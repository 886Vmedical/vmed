package com.mediatek.mt6381eco.utils;

import android.support.annotation.Nullable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MTextUtils {
  private static final SimpleDateFormat FORMATTER_DATATIME =
      new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US);
  //modify by herman
  private static final SimpleDateFormat FORMATTER_DATA =  new SimpleDateFormat("yyyy/MM/dd", Locale.US);
  //private static final SimpleDateFormat FORMATTER_DATA =  new SimpleDateFormat("yyyy-MM-dd", Locale.US);
  private static final SimpleDateFormat FORMATTER_TIME = new SimpleDateFormat("HH:mm", Locale.US);

  public static String formatDate(int year, int monthOfYear, int dayOfMonth) {
    Calendar calendar = Calendar.getInstance();
    calendar.set(year, monthOfYear, dayOfMonth);
    return formatDate(calendar.getTime());
  }

  public static String formatDateTime(Date date) {
    return FORMATTER_DATATIME.format(date);
  }

  public static String formatDate(Date date) {
    return FORMATTER_DATA.format(date);
  }

  public static String formatTime(Date date) {
    return FORMATTER_TIME.format(date);
  }

  public static String format(float d) {
    if (d == (int) d) {
      return String.format(Locale.US, "%d", (int) d);
    } else {
      return String.format("%s", d);
    }
  }

  public static boolean isEmpty(@Nullable CharSequence str) {
    return str == null || str.length() == 0;
  }

  public static Date parseDate(String dateStr) throws ParseException {
    return FORMATTER_DATA.parse(dateStr);
  }

  public static int indexOf(String[] dataArray, String text) {
    for (int i = 0; text != null && i < dataArray.length; ++i) {
      if (text.equals(dataArray[i])) {
        return i;
      }
    }
    return -1;
  }
}
