package com.mediatek.mt6381eco.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class TemperatureSQLiteHelper extends SQLiteOpenHelper {
    private final static String DB_NAME = "tempDT.db";
    private final static int DB_VERSION = 1;


    public TemperatureSQLiteHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String sqlStr = "CREATE TABLE IF NOT EXISTS tempDT (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, fulldate TEXT, tpdata REAL);";
        db.execSQL(sqlStr);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}