package com.mediatek.mt6381eco.db.entries;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import java.util.ArrayList;

@Entity public class Calibration {
  public ArrayList<Integer> getCalibrateData() {
    return calibrateData;
  }

  public void setCalibrateData(ArrayList<Integer> calibrateData) {
    this.calibrateData = calibrateData;
  }

  public int getUid() {
    return uid;
  }

  public void setUid(int uid) {
    this.uid = uid;
  }

  ArrayList<Integer> calibrateData = new ArrayList<Integer>();
  @PrimaryKey private int uid = 0;
}
