package com.mediatek.mt6381eco.biz.utlis;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.IdRes;
import com.mediatek.mt6381eco.BuildConfig;
import com.mediatek.mt6381eco.R;
import com.mediatek.mt6381eco.biz.measure.SignalChecker;
import com.mediatek.mt6381eco.utils.MTimeUtils;
import io.reactivex.Single;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class BizUtils {

  public static final String FORMATTER_ZIP_FILE_NAME = "'ZIP_FILE_'yyyy_MM_dd_HH_mm_ss_SSS'.gz'";
  public static final String FORMATTER_UNZIP_FILE_NAME = "'ZIP_FILE_'yyyy_MM_dd_HH_mm_ss_SSS'.txt'";
  private static final String WIGHT_UNIT_JIN = "jin";
  private static final String WIGHT_UNIT_LB = "lb";
  private static final String HIGHT_UNIT_INCH = "inch";
  private static final String LANGUAGE_EN = "EN";
  private static final String LANGUAGE_CN = "CN";
  private static final String LANGUAGE_TW = "TW";

  public static String getHeartRateRiskText(Context context, Integer riskLevel,
      Integer riskProbability) {
    if (riskLevel != null && riskProbability != null) {
      String[] map = context.getResources().getStringArray(R.array.heart_rate_text_map);
      if (riskLevel < map.length) {
        return String.format(map[riskLevel], riskProbability);
      }
    }
    return context.getString(R.string.empty_value);
  }

  public static int getWeight(int weight, String weightUnit) {
    double result = weight;
    switch (weightUnit) {
      case WIGHT_UNIT_JIN:
        result = weight * 0.5;
        break;
      case WIGHT_UNIT_LB:
        result = weight * 0.4535924;
        break;
    }
    return Integer.parseInt(new DecimalFormat("0").format(result));
  }

  public static String getHtmlFileName(String fileFormatter) {
    String fileName;
    String lang = Locale.getDefault().toLanguageTag();
    if (lang.contains(LANGUAGE_CN)) {
      fileName = String.format(fileFormatter, LANGUAGE_CN);
    } else if (lang.contains(LANGUAGE_TW)) {
      fileName = String.format(fileFormatter, LANGUAGE_TW);
    } else {
      fileName = String.format(fileFormatter, LANGUAGE_EN);
    }
    return fileName;
  }

  public static int getAge(Long birthday) {
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(birthday);
    int age = MTimeUtils.getCurrentYear() - cal.get(Calendar.YEAR);
    return age;
  }

  public static File gzipFile(File sourceFile) throws IOException {
    FileInputStream inputStream = new FileInputStream(sourceFile);
    File zipFile = genCacheFile(BizUtils.FORMATTER_ZIP_FILE_NAME);
    GZIPOutputStream outputStream = new GZIPOutputStream(new FileOutputStream(zipFile));
    saveFile(inputStream, outputStream);
    return zipFile;
  }

  public static File gunzipFile(File zipFile) throws IOException {
    GZIPInputStream inputStream = new GZIPInputStream(new FileInputStream(zipFile));
    File unzipFile = genCacheFile(BizUtils.FORMATTER_UNZIP_FILE_NAME);
    FileOutputStream outputStream = new FileOutputStream(unzipFile);
    saveFile(inputStream, outputStream);
    return unzipFile;
  }

  public static File saveZipFile(InputStream inputStream) throws IOException {
    File zipFile = genCacheFile(BizUtils.FORMATTER_ZIP_FILE_NAME);
    FileOutputStream outputStream = new FileOutputStream(zipFile);
    saveFile(inputStream, outputStream);
    return zipFile;
  }

  private static void saveFile(InputStream inputStream, OutputStream outputStream)
      throws IOException {
    byte[] buffer = new byte[1024];
    int len;
    while ((len = inputStream.read(buffer)) > 0) {
      outputStream.write(buffer, 0, len);
    }
    inputStream.close();
    outputStream.close();
  }

  private static File genCacheFile(String fileFormat) throws IOException {
    File file = new File(String.format("%s/cachelog/%s/",
        Environment.getExternalStorageDirectory().getAbsolutePath(), BuildConfig.APPLICATION_ID),
        new SimpleDateFormat(fileFormat, Locale.getDefault()).format(System.currentTimeMillis()));
    if (!file.exists()) {
      file.getParentFile().mkdirs();
      file.createNewFile();
    }
    return file;
  }

  public static  int getBadSignalStringIdRes(int reason){
    int idResReason = 0;
    if((reason & SignalChecker.SIGNAL_FINGER_EKG) > 0){
      idResReason = R.string.signal_finger_off_ecg;
    }else if((reason & SignalChecker.SIGNAL_FINGER_PPG1)>0){
      idResReason = R.string.signal_finger_off_ppg;
    }else if((reason & SignalChecker.SIGNAL_QUALITY_EKG)>0){
      idResReason = R.string.signal_quality_low_ecg;
    }else if((reason & SignalChecker.SIGNAL_QUALITY_PPG1)>0 ){
      idResReason = R.string.signal_quality_low_ppg;
    }else if((reason & SignalChecker.SIGNAL_QUALITY_PPG2)>0 ){
      idResReason = R.string.signal_quality_low_ppg2;
    }
    return idResReason;
  }
}

