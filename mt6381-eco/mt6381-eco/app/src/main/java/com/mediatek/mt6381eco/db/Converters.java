package com.mediatek.mt6381eco.db;

import android.arch.persistence.room.TypeConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class Converters {
  private static final Gson  s_gson = new Gson();
  @TypeConverter public static ArrayList<Integer> fromString(String value) {
    Type listType = new TypeToken<ArrayList<Integer>>() {
    }.getType();
    return s_gson.fromJson(value, listType);
  }

  @TypeConverter public static String fromArrayLisr(ArrayList<Integer> list) {
    Gson gson = new Gson();
    String json = s_gson.toJson(list);
    return json;
  }
}