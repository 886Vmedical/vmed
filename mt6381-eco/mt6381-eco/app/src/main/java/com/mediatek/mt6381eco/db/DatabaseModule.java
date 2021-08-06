package com.mediatek.mt6381eco.db;

import android.app.Application;
import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.migration.Migration;
import com.mediatek.mt6381eco.BuildConfig;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

@Module public class DatabaseModule {
  public static final String DB_NAME = "room";

  @Singleton @Provides AppDatabase provideAppDatabase(Application application) {
    return Room.databaseBuilder(application, AppDatabase.class, DB_NAME)
        .allowMainThreadQueries()
        .fallbackToDestructiveMigration()
        .build();
  }

  @Singleton @Provides EasyDao provideEasyDao(AppDatabase appDatabase){
    return new EasyDao(appDatabase.jsonDao());
  }
}
