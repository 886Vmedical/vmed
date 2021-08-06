package com.mediatek.mt6381eco.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import com.mediatek.mt6381eco.db.entries.Profile;
import io.reactivex.Flowable;

@Dao public interface ProfileDao {
  @Query("SELECT * FROM profile WHERE uid = 0 LIMIT 1") Profile findProfile();

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertProfile(Profile profile);

  @Delete
  void deleteProfile(Profile profile);
}
