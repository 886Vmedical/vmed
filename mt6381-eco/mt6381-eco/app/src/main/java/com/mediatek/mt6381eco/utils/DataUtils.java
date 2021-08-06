package com.mediatek.mt6381eco.utils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class DataUtils {
  public static int indexOf(int[] array, int value) {
    for (int i = 0; i < array.length; ++i) {
      if (value == array[i]) {
        return i;
      }
    }
    return -1;
  }

  public static int indexOf(String[] array, String text) {
    for (int i = 0; i < array.length; ++i) {
      if (text.equals(array[i])) {
        return i;
      }
    }
    return -1;
  }

  public static String arrayListToString(ArrayList arrayList, char split) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < arrayList.size(); ++i) {
      if (i > 0) {
        sb.append(split);
      }
      Object item = arrayList.get(i);
      if (item instanceof String) {
        sb.append((String) item);
      } else {
        sb.append(item);
      }
    }
    return sb.toString();
  }

  public static int[] pollToArray(ArrayDeque<Integer> list, int count) {
    int[] ret = new int[count];
    for (int i = 0; i < count; ++i) {
      ret[i] = list.poll();
    }
    return ret;
  }

  public static int[] and(int[] array1, int[] array2) {
    List<Integer> tempList = new ArrayList<>();
    for (int i = 0; i < array1.length; ++i) {
      int index = indexOf(array2, array1[i]);
      if (index > -1) {
        tempList.add(array1[i]);
      }
    }
    int[] ret = new int[tempList.size()];
    for (int i = 0; i < ret.length; ++i) {
      ret[i] = tempList.get(i);
    }
    return ret;
  }
}
