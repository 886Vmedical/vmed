package com.mediatek.mt6381eco.network.model;

/**
 * Created by MTK40526 on 12/20/2017.
 */

public class FirmwareResponse {
  public  String version;
  public FWFile[] files;

  public static class FWFile{
    public String fileName;
    public String filePath;
  }
}
