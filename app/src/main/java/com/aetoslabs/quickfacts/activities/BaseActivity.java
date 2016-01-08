package com.aetoslabs.quickfacts.activities;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import com.aetoslabs.quickfacts.core.DbOpenHelper;
import com.aetoslabs.quickfacts.core.User;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by anthony on 29/12/15.
 */
public class BaseActivity extends AppCompatActivity implements BaseContext{

    public static final String TAG = MainActivity.class.getSimpleName();
    public static final String APP_SESSION = "QuickFactSession";
    public static final String PARAM_USER_NAME = "USER_NAME";
    public static final String PARAM_USER_EMAIL = "USER_EMAIL";
    public static final String PARAM_USER_ID = "USER_ID";

    protected ProgressDialog mProgressDialog;

    protected SharedPreferences session;
    protected RequestQueue queue;
    protected DbOpenHelper mFactDbHelper;
    protected BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            onReceiveBroadcast(context, intent);
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProgressDialog = new ProgressDialog(this);
        queue = Volley.newRequestQueue(this);
        session = getSharedPreferences(APP_SESSION, Context.MODE_PRIVATE);
        mFactDbHelper = new DbOpenHelper(this);
    }

    public SharedPreferences getPrefs() {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            return getSharedPreferences(currentUser.email, Context.MODE_PRIVATE);
        } else {
            return PreferenceManager.getDefaultSharedPreferences(this);
        }
    }

    public boolean isLoggedIn(){
        return session.contains(PARAM_USER_ID) && session.getInt(PARAM_USER_ID, -1) != -1
                && session.contains(PARAM_USER_EMAIL) && !session.getString(PARAM_USER_EMAIL, "").isEmpty();
    }

    public User getCurrentUser() {
        if (!isLoggedIn()) return null;
        return new User().findById(this, session.getInt(PARAM_USER_ID, -1));
    }

    public void onReceiveBroadcast(Context context, Intent intent) {

    }

    public SharedPreferences getSession() {
        return session;
    }

    public SQLiteDatabase getWritableDb() {
        return mFactDbHelper.getWritableDatabase();
    }

    public SQLiteDatabase getReadableDb() {
        return mFactDbHelper.getReadableDatabase();
    }


}
