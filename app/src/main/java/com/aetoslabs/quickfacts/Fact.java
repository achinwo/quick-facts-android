package com.aetoslabs.quickfacts;

import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by anthony on 13/12/15.
 */
public class Fact {

    protected String content;

    @SerializedName("user_id")
    protected
    @Nullable
    Integer userId;

    @SerializedName("created_at")
    protected String createdAt;

    @SerializedName("updated_at")
    protected String updatedAt;

    public Fact(String content, @Nullable Integer userId) {
        this.content = content;
        this.userId = userId;
        updatedAt = "";
    }

    public Fact(String content){
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
