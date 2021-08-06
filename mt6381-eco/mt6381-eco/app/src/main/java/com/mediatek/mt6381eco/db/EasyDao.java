package com.mediatek.mt6381eco.db;

import com.mediatek.mt6381eco.db.entries.JsonObject;
import com.mediatek.mt6381eco.utils.JsonUtils;

public class EasyDao {

  private final JsonDao mJsonDao;

  public EasyDao(JsonDao jsonDao) {
    mJsonDao = jsonDao;
  }

  public void save(Object obj) {
    save(obj, obj.getClass().getName());
  }

  public void save(Object obj, String key) {
    JsonObject entry = new JsonObject();
    String clsName = obj.getClass().getName();
    entry.setKey(key);
    entry.setClsName(clsName);
    entry.setJson(JsonUtils.toJson(obj));
    mJsonDao.insertJsonObject(entry);
  }

  public <T> T find(Class<T> cls) {
    return find(cls.getName());
  }

  public <T> T find(String key) {
    JsonObject jsonObject = mJsonDao.findJsonObject(key);
    if(jsonObject == null) return null;
    String json = jsonObject.getJson();
    try {
      Class<T> cls = (Class<T>) Class.forName(jsonObject.getClsName());
      return JsonUtils.fromJson(json, cls);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      return null;
    }
  }
  public void delete(Class cls){
    mJsonDao.delete(cls.getName());
  }

}
