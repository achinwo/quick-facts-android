package com.aetoslabs.quickfacts.core;

import android.support.annotation.Nullable;

import java.io.Serializable;

/**
 * Created by anthony on 17/12/15.
 */
public class User implements Serializable {

    public String name;
    public String email;
    public String password;
    @Nullable
    public
    Integer id;

    @Override
    public String toString() {
        return "user(id=" + id + ", name=" + name + ", email=" + email + ")";
    }
}
