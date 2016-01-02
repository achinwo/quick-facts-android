package com.aetoslabs.quickfacts.activities;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by anthony on 02/01/16.
 */
public interface BaseContext {
    SQLiteDatabase getWritableDb();

    SQLiteDatabase getReadableDb();

    void sendBroadcast(Intent intent);
}
