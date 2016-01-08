package com.aetoslabs.quickfacts.core;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.aetoslabs.quickfacts.activities.BaseActivity;
import com.aetoslabs.quickfacts.activities.MainActivity;
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

    public boolean login(BaseActivity activity) {
        if (id == null) return false;
        SharedPreferences.Editor editor = activity.getSession().edit();
        editor.putString(MainActivity.PARAM_USER_NAME, name);
        editor.putString(MainActivity.PARAM_USER_EMAIL, email);
        editor.putInt(MainActivity.PARAM_USER_ID, id);

        editor.apply();
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof User)) {
            return false;
        } else {
            User other = (User) o;
            return other.email.equals(email) && ((other.id == null && id == null) | (other.id != null && other.id.equals(id)));
        }
    }

    public boolean logout(BaseActivity activity) {
        if (!this.equals(activity.getCurrentUser())) return false;
        SharedPreferences.Editor editor = activity.getSession().edit();
        editor.clear();
        editor.apply();
        return true;
    }

}
