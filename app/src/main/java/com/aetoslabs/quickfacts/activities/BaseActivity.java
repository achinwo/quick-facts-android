package com.aetoslabs.quickfacts.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import com.aetoslabs.quickfacts.core.FactOpenHelper;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by anthony on 29/12/15.
 */
public class BaseActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    public static final String APP_SESSION = "QuickFactSession";
    public static final String PARAM_USER_NAME = "USER_NAME";
    public static final String PARAM_USER_EMAIL = "USER_EMAIL";
    public static final String PARAM_USER_ID = "USER_ID";
    public static final String PARAM_USER = "USER_OBJ";

    protected ProgressDialog progressDialog;

    protected SharedPreferences session, prefs;
    protected RequestQueue queue;
    protected FactOpenHelper mFactDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressDialog = new ProgressDialog(this);
        queue = Volley.newRequestQueue(this);
        session = getSharedPreferences(APP_SESSION, Context.MODE_PRIVATE);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mFactDbHelper = new FactOpenHelper(this);
    }

    public SQLiteDatabase getWritableDb() {
        return mFactDbHelper.getWritableDatabase();
    }

    public SQLiteDatabase getReadableDb() {
        return mFactDbHelper.getReadableDatabase();
    }


}
