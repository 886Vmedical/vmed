package com.mediatek.mt6381eco.db;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.mediatek.mt6381eco.biz.temp.TempPresenter;

import java.util.ArrayList;
import java.util.List;

public class TempDataBaseOperation {

    TemperatureSQLiteHelper mTempSQLiteHelper;
    SQLiteDatabase recordsDb;
    SQLiteDatabase recordsdataDb;
    String tableName;
    Context context;


    public TempDataBaseOperation(Context context, String tableName){
        mTempSQLiteHelper = new TemperatureSQLiteHelper(context);
        this.tableName = tableName;
    }


    //添加记录
    public void addRecords(String Date,String record) {
            recordsDb = mTempSQLiteHelper.getReadableDatabase();
            ContentValues values = new ContentValues();
            values.put("fulldate", Date);
            values.put("tpdata", record);
            recordsDb.insert(tableName,null, values);
            recordsDb.close();
    }

    public boolean isHasRecord(String record) {
        boolean isHasRecord = false;
        recordsDb = mTempSQLiteHelper.getReadableDatabase();
        Cursor cursor = recordsDb.query(tableName, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            if (String.valueOf(record).equals(cursor.getString(cursor.getColumnIndexOrThrow("tpdata")))) {
                isHasRecord = true;
            }
        }
        recordsDb.close();
        return isHasRecord;
    }
    public List<String> getRecordsList() {
        List<String> recordsList = new ArrayList<>();
        recordsDb = mTempSQLiteHelper.getReadableDatabase();
        Cursor cursor = recordsDb.query(tableName, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow("tpdata"));
            recordsList.add(name);
        }
        return recordsList;
    }

    public List<String> getDateList() {
        List<String> recordsDate = new ArrayList<>();
        recordsdataDb = mTempSQLiteHelper.getReadableDatabase();
        Cursor cursor = recordsDb.query(tableName, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            String date = cursor.getString(cursor.getColumnIndexOrThrow("fulldate"));
            recordsDate.add(date);
        }
        recordsdataDb.close();
        return recordsDate;
    }


    public void deleteAllRecords() {
        recordsDb = mTempSQLiteHelper.getWritableDatabase();
        recordsDb.execSQL("delete from "+tableName);

        recordsDb.close();
    }
}
