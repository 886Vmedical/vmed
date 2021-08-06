package com.mediatek.blenativewrapper;

import java.util.HashMap;
import java.util.UUID;

public class UUIDNameMapping {

  private static final String NAME_UNKNOWN ="Unknown";
  private static UUIDNameMapping sInstance = null;
  private final HashMap<UUID, String> mNameMapping = new HashMap<>();
  public static UUIDNameMapping getDefault(){
    if(null == sInstance){
      sInstance = new UUIDNameMapping();
    }
    return sInstance;
  }

  public void register(UUID uuid, String name){
    mNameMapping.put(uuid, name);
  }
  public String nameOf(UUID uuid){
    String name = mNameMapping.get(uuid);
    if(null != name){
      return name;
    }
    return uuid.toString();
  }
}
