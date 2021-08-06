package com.mediatek.mt6381eco.utils;

import com.mediatek.mt6381eco.db.entries.Profile;
import com.mediatek.mt6381eco.network.model.ProfileRes;
import java.text.ParseException;

public class MappingUtils {

  public static Profile toDbEntry(ProfileRes profileResponse) throws ParseException {
    Profile ret = new Profile();
    ret.setUniqueId(profileResponse.uniqueId);
    ret.setProfileId(profileResponse.profileId);
    ret.setGender(-1);
    if (ProfileRes.GENDER_MALE == profileResponse.gender) {
      ret.setGender(com.mediatek.mt6381eco.db.entries.Profile.GENDER_MALE);
    } else if (ProfileRes.GENDER_FEMALE == profileResponse.gender) {
      ret.setGender(com.mediatek.mt6381eco.db.entries.Profile.GENDER_FEMALE);
    }
    ret.setNickName(profileResponse.name);
    ret.setBirthday(MTextUtils.parseDate(profileResponse.birthday).getTime());
    ret.setWeight(profileResponse.weight);
    ret.setHeight(profileResponse.height);
    ret.setCalibrated(profileResponse.calibrated);
    ret.setHeightUnit(profileResponse.heightUnit);
    ret.setWeightUnit(profileResponse.weightUnit);


    switch (profileResponse.personalStatus) {
      case ProfileRes.PERSONAL_STATUS_NONE: {
        ret.setPersonalStatus(Profile.PERSONAL_STATUS_NONE);
        break;
      }
      case ProfileRes.PERSONAL_STATUS_HYPERTENSION: {
        ret.setPersonalStatus(Profile.PERSONAL_STATUS_HYPERTENSION);
        break;
      }
      case ProfileRes.PERSONAL_STATUS_HYPOTENSION: {
        ret.setPersonalStatus(Profile.PERSONAL_STATUS_HYPOTENSION);
        break;
      }
    }
    ret.setTakeMedicineTime(profileResponse.takeMedicineTime);

    return ret;
  }
}
