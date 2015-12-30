package com.aetoslabs.quickfacts.core;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import com.aetoslabs.quickfacts.activities.BaseActivity;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by anthony on 29/12/15.
 */
public abstract class DbObject {

    private static final String TAG = DbObject.class.getSimpleName();

    @SerializedName("created_at")
    public String createdAt;

    @SerializedName("updated_at")
    public String updatedAt;

    public Boolean deleted = false;

    public String[] getColumnFieldNames() {
        return new String[]{"deleted", "updatedAt", "createdAt"};
    }

    public interface Reader<T extends DbObject> {
        ArrayList<Fact> readAll(BaseActivity activity);
    }

    public Boolean isDeleted() {
        return deleted;
    }

    public boolean delete(BaseActivity activity) {
        if (isNew(activity)) return false;

        SQLiteDatabase writableDb = activity.getWritableDb();
        Log.d(TAG, "deleting " + this);

        Integer id = getId();
        String attrName = "deleted";
        ContentValues values = new ContentValues();
        values.put(FactOpenHelper.COLUMN_NAME_FACT_ID, id);
        values.put(FactOpenHelper.COLUMN_NAME_ATTR, attrName);
        values.put(FactOpenHelper.COLUMN_NAME_VALUE, "true");

        String selection = FactOpenHelper.COLUMN_NAME_FACT_ID + " = ? AND " +
                FactOpenHelper.COLUMN_NAME_ATTR + " = ?";
        String[] selectionArgs = {String.valueOf(id), attrName};

        int count = writableDb.update(getTableName(), values, selection, selectionArgs);
        if (count > 0) {
            deleted = true;
            Log.d(TAG, "Deleted (" + count + "): " + toString());
        } else {
            Log.e(TAG, "Deleted Failed: " + toString());
        }
        return isDeleted();
    }

    public boolean isNew(BaseActivity activity) {
        boolean isNew = true;
        Integer id = getId();
        if (id != null) {
            SQLiteDatabase writableDb = activity.getWritableDb();
            SQLiteQueryBuilder query = new SQLiteQueryBuilder();
            query.setTables(getTableName());
            query.appendWhere("id=" + id.toString());
            Cursor cursor = query.query(writableDb, null, null, null, null, null, null);

            isNew = cursor.getCount() == 0;

            cursor.close();
            writableDb.close();
        }
        return isNew;
    }

    public Integer getId() {
        return null;
    }

    public boolean write(BaseActivity activity) {
        SQLiteDatabase writableDb = activity.getWritableDb();
        List<String> colNames = Arrays.asList(getColumnFieldNames());

        Integer id = getId();
        Log.d(TAG, "Fact" + toString());
        SQLiteQueryBuilder query = new SQLiteQueryBuilder();
        query.setTables(getTableName());
        query.appendWhere("id=" + (id == null ? "null" : id.toString()));

        Cursor cursor = query.query(writableDb, null, null, null, null, null, null);

        cursor.moveToFirst();

        List<String> existingCols = new ArrayList<>();
        int valueColIdx = cursor.getColumnIndex(FactOpenHelper.COLUMN_NAME_ATTR);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            existingCols.add(cursor.getString(valueColIdx));
        }

        Log.d(TAG, "Number of existing entries =" + cursor.getCount() + " Existing=" + existingCols);
        cursor.close();

        boolean successful = true;
        String attrName = null, strValue = null;
        for (Field field : getClass().getFields()) {
            if (!colNames.contains(field.getName())) {
                Log.d(TAG, "Excluding field " + field.getName());
                continue;
            }

            try {
                attrName = field.getName();
                strValue = field.get(this) == null ? null : String.valueOf(field.get(this));

                ContentValues values = new ContentValues();
                values.put(FactOpenHelper.COLUMN_NAME_FACT_ID, id);
                values.put(FactOpenHelper.COLUMN_NAME_ATTR, attrName);
                values.put(FactOpenHelper.COLUMN_NAME_VALUE, strValue);

                if (existingCols.contains(attrName)) {
                    String selection = FactOpenHelper.COLUMN_NAME_FACT_ID + " = ? AND " +
                            FactOpenHelper.COLUMN_NAME_ATTR + " = ?";
                    String[] selectionArgs = {String.valueOf(id), attrName};

                    int count = writableDb.update(getTableName(), values, selection, selectionArgs);
                } else {
                    writableDb.insert(getTableName(), FactOpenHelper.COLUMN_NAME_VALUE, values);
                }

            } catch (IllegalAccessException e) {
                successful = false;
                Log.e(TAG, "Error attr=" + attrName + " value=" + strValue);
                Log.e(TAG, "Error writing fact: " + e);
            }
        }
        writableDb.close();
        return successful;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(getClass().getSimpleName()).append("{");
        for (Field field : getClass().getFields()) {
            try {
                b.append(String.format("%s=%s, ", field.getName(), field.get(this)));
            } catch (IllegalAccessException e) {
                Log.e(TAG, "toString Error: " + e);
            }
        }
        return b.append("}").toString();
    }

    public String getTableName() {
        return null;
    }
}
