package com.mediatek.mt6381eco.utils;

import com.google.gson.Gson;

public class JsonUtils {
  private static final Gson sGson = new Gson();

  public static String toJson(Object src) {
    return sGson.toJson(src);
  }

  public static <T> T fromJson(String json, Class<T> classOfT) {
    return sGson.fromJson(json, classOfT);
  }

  public static Object fromJson(String json, String clsName) throws ClassNotFoundException {
    Class<?> cls = Class.forName(clsName);
    return sGson.fromJson(json, cls);
  }
}
