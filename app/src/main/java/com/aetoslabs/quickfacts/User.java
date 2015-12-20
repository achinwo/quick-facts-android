package com.aetoslabs.quickfacts;

import android.support.annotation.Nullable;

import java.io.Serializable;

/**
 * Created by anthony on 17/12/15.
 */
public class User implements Serializable {

    protected String name, email, password;
    protected
    @Nullable
    Integer id;

    @Override
    public String toString() {
        return "user(id=" + id + ", name=" + name + ", email=" + email + ")";
    }
}
