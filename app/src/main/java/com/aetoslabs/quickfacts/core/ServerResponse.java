package com.aetoslabs.quickfacts.core;

import android.util.Log;

import com.aetoslabs.quickfacts.activities.BaseContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by anthony on 02/01/16.
 */
public class ServerResponse implements Serializable {

    private static String TAG = ServerResponse.class.getSimpleName();


    public HashMap<String, String[]> errors;
    public ArrayList<Fact> facts;
    public ArrayList<User> users;
    public Fact fact;
    public User user;

    public void writeAll(BaseContext baseContext) {
        for (Fact fact : facts) {
            fact.write(baseContext);
        }

        for (User user : users) {
            user.write(baseContext);
        }

        Log.d(TAG, "writeAll: wrote " + facts.size() + " fact(s) and " + users.size() + " user(s)");
    }

    public void addError(Throwable e) {
        if (errors == null) errors = new HashMap<>();
        errors.put(e.getClass().getSimpleName(), new String[]{e.getMessage()});
    }
}
