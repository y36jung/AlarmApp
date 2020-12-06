package com.midmad1.alarmapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.midmad1.alarmapp.AlarmContract.AlarmEntry;

public class AlarmDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME  = "alarmlist.db";
    public static final int DATABASE_VERSION = 1;

    public AlarmDBHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_ALARMLIST_TABLE = "CREATE TABLE " +
                AlarmEntry.TABLE_NAME + " (" +
                AlarmEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                AlarmEntry.COLUMN_EVENT + " TEXT NOT NULL, " +
                AlarmEntry.COLUMN_ALARM + " LONG NOT NULL,  " +
                AlarmEntry.COLUMN_VIBRATION + " INTEGER NOT NULL, " +
                AlarmEntry.COLUMN_SOUND + " INTEGER NOT NULL " +
                ");";
        db.execSQL(SQL_CREATE_ALARMLIST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + AlarmEntry.TABLE_NAME);
        onCreate(db);
    }
}
