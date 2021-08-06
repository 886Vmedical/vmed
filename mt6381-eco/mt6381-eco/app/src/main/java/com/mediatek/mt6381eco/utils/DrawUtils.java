package com.mediatek.mt6381eco.utils;

import android.graphics.Paint;
import android.graphics.Rect;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;

public class DrawUtils {
  
  public static float getTextHeight(String text, Paint paint) {
    return getTextBounds(text, paint).height();
  }

  public static float getTextWidth(String text, Paint paint) {
    return getTextBounds(text, paint).width();
  }

  private static Rect getTextBounds(String text, Paint paint) {
    Rect textBounds = new Rect();
    paint.setAntiAlias(true);
    paint.getTextBounds(text, 0, text.length(), textBounds);
    return textBounds;
  }

  /**
   * Return the minimum threshold of given numbers.
   * Each step is pre-defined as 10.
   * @param numbers
   * @return min threshold
   */
  public static int getMinThreshold(Integer[] numbers) {
    final int STEP = 10;
    final int NULL_STATE = -1;

    float min = numbers[0];
    for (int i = 1; i < numbers.length; i++) {
      if (min == NULL_STATE) {
        min = numbers[i];
      } else {
        min = ((numbers[i] < min) && (numbers[i] > 0)) ? numbers[i] : min;
      }
    }
    return ((int)Math.floor(min / STEP)) * STEP;
  }

  /**
   * Return the maximum threshold of given numbers.
   * Each step is pre-defined as 10.
   * @param numbers
   * @return max threshold
   */
  public static int getMaxThreshold(Integer[] numbers) {
    final int STEP = 10;
    float max = (float) Collections.max(Arrays.asList(numbers));
    return (int) Math.ceil(max / STEP) * STEP;
  }

  public static float getNumberToKilo(int maxNumber) {
    final int UNIT_CONVETER_STEP = 1000;
    return ((float) maxNumber / UNIT_CONVETER_STEP);
  }

  /**
   * converter kilometer to meter
   * @param kilometer
   * @return
   */
  public static int getKmToM(float kilometer) {
    final int UNIT_CONVETER_STEP = 1000;
    return (int) (kilometer * UNIT_CONVETER_STEP);
  }

  /**
   * Return the string, with the minimum necessary precision
   * for example, 4.0 (float) to "4" (String)
   * @param number
   * @return
   */
  public static String getFloatText(float number) {
    DecimalFormat df = new DecimalFormat("##.#");
    return df.format(number);
  }

  public static int calGradiantColor(int startElementColor, int endElementColor, float position, float totalLength) {
    return (int)((startElementColor - endElementColor) * Math.abs(position) / totalLength) + endElementColor;
  }

}
