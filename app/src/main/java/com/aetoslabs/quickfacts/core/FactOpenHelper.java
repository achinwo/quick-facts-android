package com.aetoslabs.quickfacts.core;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by anthony on 24/12/15.
 */
public class FactOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    public static final String FACTS_TABLE_NAME = "facts";
    public static final String COLUMN_NAME_ATTR = "attribute";
    public static final String COLUMN_NAME_FACT_ID = "id";
    public static final String COLUMN_NAME_VALUE = "value";
    public static final String DATABASE_NAME = "quick_facts.db";
    public static final String FACTS_TABLE_INDEX_NAME = "index_id_and_attribute";
    private static final String FACTS_TABLE_DELETE =
            "DROP TABLE IF EXISTS " + FACTS_TABLE_NAME + ";";
    private static final String FACTS_TABLE_CREATE =
            "CREATE TABLE " + FACTS_TABLE_NAME + " (" +
                    COLUMN_NAME_FACT_ID + " INTEGER, " +
                    COLUMN_NAME_ATTR + " VARCHAR(255), " +
                    COLUMN_NAME_VALUE + " TEXT);";
    private static final String FACTS_TABLE_INDEX_CREATE =
            "CREATE UNIQUE INDEX " + FACTS_TABLE_INDEX_NAME + " " +
                    "ON " + FACTS_TABLE_NAME + " (" +
                    COLUMN_NAME_FACT_ID + ", " + COLUMN_NAME_ATTR + ");";

    public FactOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(FACTS_TABLE_CREATE);
        db.execSQL(FACTS_TABLE_INDEX_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(FACTS_TABLE_DELETE);
        onCreate(db);
    }

}
