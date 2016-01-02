package com.aetoslabs.quickfacts.core;

import android.support.annotation.Nullable;

import com.google.common.collect.ObjectArrays;

/**
 * Created by anthony on 17/12/15.
 */
public class User extends DbObject {

    public String name;
    public String email;
    public String password;
    public String password_digest;
    @Nullable
    public
    Integer id;

    public String[] getColumnFieldNames() {
        return ObjectArrays.concat(super.getColumnFieldNames(), new String[]{"name", "email", "password_digest"}, String.class);
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public String getTableName() {
        return "users";
    }

}
