package com.aetoslabs.quickfacts.core;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.util.Log;

import com.aetoslabs.quickfacts.activities.BaseActivity;
import com.google.common.collect.ObjectArrays;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by anthony on 13/12/15.
 */
public class Fact extends DbObject implements DbObject.Reader {
    private final static String TAG = Fact.class.getSimpleName();
    public static String DB_TABLE_NAME = "facts";

    public String content;
    @Nullable
    public Integer id;

    @SerializedName("user_id")
    public
    @Nullable
    Integer userId;

    @Override
    @Nullable
    public Integer getId() {
        return id;
    }

    public String getTableName() {
        return DB_TABLE_NAME;
    }

    public Fact() {

    }

    public Fact(String content, @Nullable Integer userId) {
        this.content = content;
        this.userId = userId;
        updatedAt = "";
    }

    public String[] getColumnFieldNames() {
        String[] parentFields = super.getColumnFieldNames();
        return ObjectArrays.concat(parentFields, new String[]{"userId", "content"}, String.class);
    }

    public Fact(String content) {
        this.content = content;
        this.userId = null;
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

    @Override
    public ArrayList<Fact> readAll(BaseActivity activity) {
        ArrayList<Fact> facts = new ArrayList<>();
        SQLiteDatabase writableDb = activity.getReadableDb();
        Cursor c = writableDb.query(true, getTableName(), new String[]{FactOpenHelper.COLUMN_NAME_FACT_ID}, null, null, null, null, null, null);
        ArrayList<Integer> ids = new ArrayList<>();
        int idColIdx = c.getColumnIndex(FactOpenHelper.COLUMN_NAME_FACT_ID);
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            Integer id = c.getInt(idColIdx);
            Cursor c2 = writableDb.query(getTableName(), null,
                    "id=?", new String[]{String.valueOf(id)}, null, null, null, null);


            int attrColIdx = c2.getColumnIndex(FactOpenHelper.COLUMN_NAME_ATTR);
            int valueColIdx = c2.getColumnIndex(FactOpenHelper.COLUMN_NAME_VALUE);
            Fact newFact = new Fact();
            newFact.id = id;
            for (c2.moveToFirst(); !c2.isAfterLast(); c2.moveToNext()) {

                String attrName = c2.getString(attrColIdx);
                String value = c2.getString(valueColIdx);
                try {
                    Field fld = Fact.class.getField(attrName);
                    if (fld.getType() == Integer.class) {
                        fld.set(newFact, value == null || value.toLowerCase().equals("null") ? null : Integer.valueOf(value));
                    } else if (fld.getType() == Boolean.class) {
                        fld.set(newFact, value == null || value.toLowerCase().equals("null") ? false : Boolean.valueOf(value));
                    } else {
                        fld.set(newFact, fld.getType().cast(value));
                    }

                } catch (NoSuchFieldException e) {
                    Log.e(TAG, "Error getting attr " + attrName + " id=" + id + " error=" + e);
                } catch (IllegalAccessException e) {
                    Log.e(TAG, "Error population value " + attrName + " id=" + id + " error=" + e);
                } catch (ClassCastException e) {
                    Log.e(TAG, "Error population attr=" + attrName + " id=" + id + " value=" + value + " error=" + e);
                }

            }
            facts.add(newFact);
            c2.close();
        }
        c.close();
        return facts;
    }
}
