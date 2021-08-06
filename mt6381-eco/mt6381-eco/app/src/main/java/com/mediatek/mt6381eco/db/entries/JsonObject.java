package com.mediatek.mt6381eco.db.entries;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity public class JsonObject {
  @PrimaryKey @NonNull private  String key;
  private String clsName;
  private String json;

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getClsName() {
    return clsName;
  }

  public void setClsName(String clsName) {
    this.clsName = clsName;
  }

  public String getJson() {
    return json;
  }

  public void setJson(String json) {
    this.json = json;
  }
}
