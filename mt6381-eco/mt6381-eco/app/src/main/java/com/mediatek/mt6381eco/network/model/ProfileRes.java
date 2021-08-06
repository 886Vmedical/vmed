package com.mediatek.mt6381eco.network.model;

import com.google.gson.annotations.Expose;

public class ProfileRes {
  //public static final String GENDER_MALE = "male";
  //public static final String GENDER_FEMALE = "female";

  public static final int GENDER_MALE = 0; //男
  public static final int GENDER_FEMALE = 1; //女

  public static final String PERSONAL_STATUS_NONE = "none";
  public static final String PERSONAL_STATUS_HYPERTENSION = "hypertension";
  public static final String PERSONAL_STATUS_HYPOTENSION = "hypotension";
  public String uniqueId;
  public String profileId;
  @Expose public String name;
  //@Expose public String gender;
  @Expose public int gender;
  @Expose public String birthday;
  @Expose public int height;
  @Expose public int weight;
  @Expose public String weightUnit;
  @Expose public String heightUnit;
  @Expose public String personalStatus;
  @Expose public Integer takeMedicineTime;
  @Expose public boolean calibrated;
}
