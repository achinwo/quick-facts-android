package com.aetoslabs.quickfacts.core;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import com.aetoslabs.quickfacts.SyncService;
import com.aetoslabs.quickfacts.activities.BaseContext;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by anthony on 29/12/15.
 */
public abstract class DbObject implements Serializable {

    private static final String TAG = DbObject.class.getSimpleName();

    @SerializedName("created_at")
    public String createdAt;

    @SerializedName("updated_at")
    public String updatedAt;

    public Boolean deleted = false;

    public String[] getColumnFieldNames() {
        return new String[]{"deleted", "updatedAt", "createdAt"};
    }

    public Date getCreatedAt() {
        return Utils.parseDate(createdAt);
    }

    public Date getUpdatedAt() {
        return Utils.parseDate(updatedAt);
    }

    public <T extends DbObject> T findById(BaseContext baseContext, Integer id) {
        T dbObj = null;
        if (id != null) {
            try (SQLiteDatabase writableDb = baseContext.getWritableDb();
                 Cursor cursor = writableDb.query(getTableName(), null,
                         "id=?", new String[]{String.valueOf(id)}, null, null, null, null)
            ) {
                if (cursor.getCount() == 0) return null;

                dbObj = ((Class<T>) this.getClass()).newInstance();
                dbObj.getClass().getField("id").set(dbObj, id);

                int attrColIdx = cursor.getColumnIndex(DbOpenHelper.COLUMN_NAME_ATTR);
                int valueColIdx = cursor.getColumnIndex(DbOpenHelper.COLUMN_NAME_VALUE);

                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    String attrName = cursor.getString(attrColIdx);
                    String value = cursor.getString(valueColIdx);
                    try {
                        Field fld = dbObj.getClass().getField(attrName);
                        if (fld.getType() == Integer.class) {
                            fld.set(dbObj, value == null || value.toLowerCase().equals("null") ? null : Integer.valueOf(value));
                        } else if (fld.getType() == Boolean.class) {
                            fld.set(dbObj, value == null || value.toLowerCase().equals("null") ? false : Boolean.valueOf(value));
                        } else {
                            fld.set(dbObj, fld.getType().cast(value));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error population attr=" + attrName + " id=" + id + " value=" + value + " error=" + e);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error setting id, " + dbObj + " id=" + id + " error=" + e);
            }
        }
        return dbObj;
    }

    public <T extends DbObject> ArrayList<T> readAll(BaseContext baseContext) {
        ArrayList<T> dbObjs = new ArrayList<>();
        try (SQLiteDatabase writableDb = baseContext.getReadableDb();
             Cursor c = writableDb.query(true, getTableName(), new String[]{DbOpenHelper.COLUMN_NAME_ID}, null, null, null, null, null, null)
        ) {
            int idColIdx = c.getColumnIndex(DbOpenHelper.COLUMN_NAME_ID);
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                dbObjs.add((T) findById(baseContext, c.getInt(idColIdx)));
            }
        }
        Log.d(TAG, "readAll (" + this.getClass().getSimpleName() + "): " + dbObjs.size());
        return dbObjs;
    }

    public Boolean isDeleted() {
        return deleted;
    }

    public boolean delete(BaseContext baseContext) {
        if (isNew(baseContext)) return false;

        Integer id = getId();
        String attrName = "deleted";
        ContentValues values = new ContentValues();
        values.put(DbOpenHelper.COLUMN_NAME_ID, id);
        values.put(DbOpenHelper.COLUMN_NAME_ATTR, attrName);
        values.put(DbOpenHelper.COLUMN_NAME_VALUE, "true");

        String selection = DbOpenHelper.COLUMN_NAME_ID + " = ? AND " +
                DbOpenHelper.COLUMN_NAME_ATTR + " = ?";
        String[] selectionArgs = {String.valueOf(id), attrName};
        Log.d(TAG, "before delete: " + this);
        try (SQLiteDatabase writableDb = baseContext.getWritableDb()) {
            deleted = writableDb.update(getTableName(), values, selection, selectionArgs) > 0;
        }
        if (deleted) {
            Intent intent = new Intent();
            intent.setAction(SyncService.ACTION_DELETE_FACT);
            intent.putExtra(SyncService.KEY_DELETED_FACT_ID, getId());
            baseContext.sendBroadcast(intent);
        } else {
            Log.e(TAG, "Deleted Failed: " + toString());
        }
        Log.d(TAG, "after delete: " + this);
        return isDeleted();
    }

    public boolean isNew(BaseContext baseContext) {
        return findById(baseContext, getId()) == null;
    }

    public abstract Integer getId();

    public boolean write(BaseContext baseContext) {
        SQLiteDatabase writableDb = baseContext.getWritableDb();
        List<String> colNames = Arrays.asList(getColumnFieldNames());
        List<String> existingCols = new ArrayList<>();

        Integer id = getId();
        SQLiteQueryBuilder query = new SQLiteQueryBuilder();
        query.setTables(getTableName());
        query.appendWhere("id=" + (id == null ? "null" : id.toString()));

        try (Cursor cursor = query.query(writableDb, null, null, null, null, null, null)) {
            cursor.moveToFirst();
            int valueColIdx = cursor.getColumnIndex(DbOpenHelper.COLUMN_NAME_ATTR);

            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                existingCols.add(cursor.getString(valueColIdx));
            }
            Log.d(TAG, "Number of existing entries: " + cursor.getCount() + " Existing=" + existingCols);
        }

        boolean successful = true;
        String attrName = null, strValue = null;
        for (Field field : getClass().getFields()) {
            if (!colNames.contains(field.getName())) continue;

            try {
                attrName = field.getName();
                strValue = field.get(this) == null ? null : String.valueOf(field.get(this)).trim();

                ContentValues values = new ContentValues();
                values.put(DbOpenHelper.COLUMN_NAME_ID, id);
                values.put(DbOpenHelper.COLUMN_NAME_ATTR, attrName);
                values.put(DbOpenHelper.COLUMN_NAME_VALUE, strValue);

                if (existingCols.contains(attrName)) {
                    String selection = DbOpenHelper.COLUMN_NAME_ID + " = ? AND " +
                            DbOpenHelper.COLUMN_NAME_ATTR + " = ?";
                    String[] selectionArgs = {String.valueOf(id), attrName};

                    writableDb.update(getTableName(), values, selection, selectionArgs);
                } else {
                    writableDb.insert(getTableName(), DbOpenHelper.COLUMN_NAME_VALUE, values);
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

    public <T extends DbObject> T getLastUpdated(BaseContext baseContext) {
        ArrayList<T> all = readAll(baseContext);
        return Collections.max(all, new Comparator<T>() {
            @Override
            public int compare(T lhs, T rhs) {
                Date d1 = lhs.getUpdatedAt(), d2 = rhs.getUpdatedAt();
                if (d1 == null && d2 == null) return 0;
                if (d1 == null || d2 == null) return d1 == null ? -1 : 1;
                return d1.compareTo(d2);
            }
        });
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(getClass().getSimpleName()).append("{");
        for (Field field : getClass().getFields()) {
            try {
                b.append(String.format("%s=%s, \n", field.getName(), field.get(this)));
            } catch (IllegalAccessException e) {
                Log.e(TAG, "toString Error: " + e);
            }
        }
        return b.append("}").toString();
    }

    public abstract String getTableName();
}
