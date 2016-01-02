package com.aetoslabs.quickfacts.core;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by anthony on 24/12/15.
 */
public class DbOpenHelper extends SQLiteOpenHelper {
    private static String TAG = DbOpenHelper.class.getSimpleName();
    private static final int DATABASE_VERSION = 1;
    public static final String COLUMN_NAME_ATTR = "attribute";
    public static final String COLUMN_NAME_ID = "id";
    public static final String COLUMN_NAME_VALUE = "value";
    public static final String DATABASE_NAME = "quick_facts.db";
    public static final String TABLE_INDEX_NAME = "index_id_and_attribute";


    public DbOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public DbObject[] getDbObjectInstances() {
        return new DbObject[]{new Fact(), new User()};
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (DbObject dbObj : getDbObjectInstances()) {
            String tableName = dbObj.getTableName();
            db.execSQL(getTableCreateStatement(tableName));
            db.execSQL(getIndexCreateStatement(tableName));
            Log.d(TAG, "onCreate: " + dbObj.getTableName() + " table");
        }
        Log.d(TAG, "onCreate: done.");
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        Log.d(TAG, "onConfigure: ");
    }

    private String getIndexCreateStatement(String tableName) {
        return "CREATE UNIQUE INDEX " + TABLE_INDEX_NAME + "_" + tableName + " " +
                "ON " + tableName + " (" +
                COLUMN_NAME_ID + ", " + COLUMN_NAME_ATTR + ");";
    }

    private String getTableCreateStatement(String tableName) {
        return "CREATE TABLE " + tableName + " (" +
                COLUMN_NAME_ID + " INTEGER, " +
                COLUMN_NAME_ATTR + " VARCHAR(255), " +
                COLUMN_NAME_VALUE + " TEXT);";
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "old version=" + oldVersion + ", new version=" + newVersion);
        for (DbObject dbObj : getDbObjectInstances()) {
            String tableName = dbObj.getTableName();
            db.execSQL("DROP TABLE IF EXISTS " + tableName);
            Log.d(TAG, "onUpgrade: dropped table = " + tableName);
        }
        onCreate(db);
    }

}
