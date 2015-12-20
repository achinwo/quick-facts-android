package com.aetoslabs.quickfacts;

import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

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

    public static void main(String[] args) {
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Map<String, String[]>>>(){}.getType();
        Map<String, Map<String, String[]>> myMap = gson.fromJson("{\"errors\":{\"email\":[\"is invalid\"]}}", type);
        System.out.println(myMap.get("errors"));

        Map<String, String[]> empty = new HashMap<String, String[]>();
        String[] emailErrors = myMap.get("errors").get("email");
        String[] passwordErrors = myMap.get("errors").get("password");
        System.out.println("Email: "+emailErrors[0]+" Pass:"+passwordErrors);
    }

}
