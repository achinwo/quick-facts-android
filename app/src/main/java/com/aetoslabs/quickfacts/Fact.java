package com.aetoslabs.quickfacts;

import com.google.gson.annotations.SerializedName;

/**
 * Created by anthony on 13/12/15.
 */
public class Fact {

    protected String content;
    protected String id;

    @SerializedName("created_at")
    protected String createdAt;

    @SerializedName("updated_at")
    protected String updatedAt;

    public Fact(String content, String id){
        this.content = content;
        this.id = id;
    }

    public Fact(String content){
        this.content = content;
        this.id = null;
    }

}
