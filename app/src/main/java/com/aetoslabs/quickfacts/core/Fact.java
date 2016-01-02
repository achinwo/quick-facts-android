package com.aetoslabs.quickfacts.core;

import android.support.annotation.Nullable;

import com.google.common.collect.ObjectArrays;
import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by anthony on 13/12/15.
 */
public class Fact extends DbObject {
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

    public String getContent() {
        return content.trim();
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
}
