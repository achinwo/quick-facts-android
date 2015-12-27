package com.aetoslabs.quickfacts.core;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by anthony on 13/12/15.
 */
public class Fact {
    private final static String TAG = Fact.class.getSimpleName();

    public String content;
    @Nullable
    public Integer id;

    @SerializedName("user_id")
    public
    @Nullable
    Integer userId;

    @SerializedName("created_at")
    public String createdAt;

    @SerializedName("updated_at")
    public String updatedAt;

    public Fact(String content, @Nullable Integer userId) {
        this.content = content;
        this.userId = userId;
        updatedAt = "";
    }

    public String[] exclude() {
        return new String[]{"id", "TAG"};
    }

    public Fact(String content){
        this.content = content;
        this.userId = null;
    }

    public boolean write(SQLiteDatabase writableDb) {
        boolean successful = true;
        Field[] allFields = Fact.class.getDeclaredFields();
        String[] exclude = exclude();
        for (Field field : allFields) {
            if (Arrays.asList(exclude).contains(field.getName())) {
                Log.d(TAG, "Excluding field " + field.getName());
                continue;
            }

            try {
                String strValue = field.get(this) == null ? null : field.get(this).toString();
                ContentValues values = new ContentValues();
                values.put(FactOpenHelper.COLUMN_NAME_FACT_ID, id);
                values.put(FactOpenHelper.COLUMN_NAME_ATTR, field.getName());
                values.put(FactOpenHelper.COLUMN_NAME_VALUE, strValue);

                writableDb.insert(
                        FactOpenHelper.FACTS_TABLE_NAME,
                        FactOpenHelper.COLUMN_NAME_VALUE,
                        values);
            } catch (IllegalAccessException e) {
                successful = false;
                Log.e(TAG, "Error writing fact: " + e);
            }


        }
        return successful;
    }

    public static void main(String[] args) {
        String fmt = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        String dtStart = "2015-12-17T16:35:22.417Z";
        SimpleDateFormat format = new SimpleDateFormat(fmt);
        try {
            Date date = format.parse(dtStart);
            System.out.println("Date ->" + date);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
