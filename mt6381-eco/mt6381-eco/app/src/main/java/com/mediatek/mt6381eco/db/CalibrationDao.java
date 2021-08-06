package com.mediatek.mt6381eco.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import com.mediatek.mt6381eco.db.entries.Calibration;

@Dao public interface CalibrationDao {
  @Query("SELECT * FROM calibration WHERE uid = 0 LIMIT 1") Calibration findCalibration();

  @Insert(onConflict = OnConflictStrategy.REPLACE) void insertCalibration(Calibration calibration);
}
