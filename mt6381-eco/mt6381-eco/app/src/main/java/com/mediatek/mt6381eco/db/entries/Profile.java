package com.mediatek.mt6381eco.db.entries;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity public class Profile {
  //modify by herman
  //public static final int GENDER_MALE = 0;
  //public static final int GENDER_FEMALE = 1;
  public static final int GENDER_MALE = 0;
  public static final int GENDER_FEMALE = 1;

  public static final int PERSONAL_STATUS_HYPOTENSION = 0;
  public static final int PERSONAL_STATUS_NONE = 1;
  public static final int PERSONAL_STATUS_HYPERTENSION = 2;
  public String weightUnit;
  public String heightUnit;
  @PrimaryKey private int uid = 0;
  private String uniqueId;
  private String profileId;
  private String nickName;
  private Integer gender;
  private Long birthday;
  private Integer height;
  private Integer weight;
  private boolean calibrated;
  private Integer personalStatus;
  private Integer takeMedicineTime;

  public Integer getPersonalStatus() {
    return personalStatus;
  }

  public void setPersonalStatus(Integer personalStatus) {
    this.personalStatus = personalStatus;
  }

  public Integer getTakeMedicineTime() {
    return takeMedicineTime;
  }

  public void setTakeMedicineTime(Integer takeMedicineTime) {
    this.takeMedicineTime = takeMedicineTime;
  }

  public String getUniqueId() {
    return uniqueId;
  }

  public void setUniqueId(String uniqueId) {
    this.uniqueId = uniqueId;
  }

  public String getHeightUnit() {
    return heightUnit;
  }

  public String getWeightUnit() {
    return weightUnit;
  }

  public void setWeightUnit(String weightUnit) {
    this.weightUnit = weightUnit;
  }
  public void setHeightUnit(String heightUnit) {
    this.heightUnit = heightUnit;
  }

  public int getUid() {
    return uid;
  }

  public void setUid(int uid) {
    this.uid = uid;
  }

  public String getProfileId() {
    return profileId;
  }

  public void setProfileId(String profileId) {
    this.profileId = profileId;
  }

  public boolean isCalibrated() {
    return calibrated;
  }

  public void setCalibrated(boolean calibrated) {
    this.calibrated = calibrated;
  }

  public Integer getGender() {
    return gender;
  }

  public void setGender(Integer gender) {
    this.gender = gender;
  }

  public Integer getHeight() { return height; }

  public void setHeight(Integer height) {
    this.height = height;
  }

  public Integer getWeight() {
    return weight;
  }

  public void setWeight(Integer weight) {
    this.weight = weight;
  }

  public Long getBirthday() {
    return birthday;
  }

  public void setBirthday(Long birthday) {
    this.birthday = birthday;
  }

  public String getNickName() {
    return nickName;
  }

  public void setNickName(String nickName) {
    this.nickName = nickName;
  }
}
