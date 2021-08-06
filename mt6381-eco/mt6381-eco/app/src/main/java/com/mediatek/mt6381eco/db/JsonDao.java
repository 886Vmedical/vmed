package com.mediatek.mt6381eco.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import com.mediatek.mt6381eco.db.entries.JsonObject;

@Dao public interface JsonDao {
  @Query("SELECT * FROM JsonObject WHERE key = :key LIMIT 1") JsonObject findJsonObject(String key);
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertJsonObject(JsonObject jsonObjectEntry);

  @Query("DELETE FROM JsonObject WHERE key = :key")
  void delete(String key);
}
