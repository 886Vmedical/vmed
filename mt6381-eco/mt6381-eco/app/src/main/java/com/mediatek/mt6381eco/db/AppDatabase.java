package com.mediatek.mt6381eco.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import com.mediatek.mt6381eco.db.entries.Calibration;
import com.mediatek.mt6381eco.db.entries.JsonObject;
import com.mediatek.mt6381eco.db.entries.Profile;

@TypeConverters({ Converters.class })
@Database(entities = { Profile.class, Calibration.class , JsonObject.class}, version = 7)
public abstract class AppDatabase extends RoomDatabase {
  public abstract ProfileDao profileDao();

  public abstract CalibrationDao calibrationDao();
  public abstract JsonDao jsonDao();
}
