package com.mediatek.mt6381eco.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MTimeUtils {

  private static final DateFormat DATE_FORMAT_MMdd = new SimpleDateFormat("MM/dd");
  private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.getDefault());
  private static final SimpleDateFormat FORMATER_TIME_MILlSECONDS =
      new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault());
  private static final SimpleDateFormat FORMATTER_TIME_GMT =
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);

  static {
    FORMATTER_TIME_GMT.setTimeZone(TimeZone.getTimeZone("GMT"));
  }

  private MTimeUtils() {
  }

  public static int getCurrentYear() {
    int ret = Calendar.getInstance().get(Calendar.YEAR);
    return ret;
  }

  public static String formatTimeInGMT(long timestamp) {
    return FORMATTER_TIME_GMT.format(timestamp);
  }

  public static String getDateMMdd(long timeStamp) {
    try {
      timeStamp = convertUnixTime(timeStamp);
      return DATE_FORMAT_MMdd.format(timeStamp);
    } catch (Exception ex) {
      return ex.getMessage();
    }
  }

  public static String getTime(long timeStamp) {
    try {
      timeStamp = convertUnixTime(timeStamp);
      return TIME_FORMAT.format(timeStamp);
    } catch (Exception ex) {
      return ex.getMessage();
    }
  }

  private static long convertUnixTime(long timeStamp) {
    if ((timeStamp + "").length() == 10) {
      timeStamp = timeStamp * 1000L;
    }
    return timeStamp;
  }

  public static int calcYear(Date date1, Date date2) {
    Calendar calender1 = Calendar.getInstance();
    calender1.setTime(date1);
    Calendar calender2 = Calendar.getInstance();
    calender2.setTime(date2);
    int year1 = calender1.get(Calendar.YEAR);
    int year2 = calender2.get(Calendar.YEAR);
    int diff = year2 - year1;
    calender1.add(Calendar.YEAR, diff);
    if (calender1.getTimeInMillis() > calender2.getTimeInMillis()) {
      --diff;
    }
    return diff;
  }

  public static int calcAge(Date birthday) {
    return calcYear(birthday, new Date());
  }

  public static long clearTime(long timeInMillis) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(timeInMillis);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    return calendar.getTimeInMillis();
  }

  public static String formatTimeMillSeconds(long timeInMillis) {
    return FORMATER_TIME_MILlSECONDS.format(timeInMillis);
  }
}
