package com.aetoslabs.quickfacts;

import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by anthony on 13/12/15.
 */
public class Fact {

    protected String content;

    @SerializedName("user_id")
    protected Integer userId;

    @SerializedName("created_at")
    protected String createdAt;

    @SerializedName("updated_at")
    protected String updatedAt;

    public Fact(String content, @Nullable Integer userId) {
        this.content = content;
        this.userId = userId;
    }

    public Fact(String content){
        this.content = content;
        this.userId = null;
    }

}
