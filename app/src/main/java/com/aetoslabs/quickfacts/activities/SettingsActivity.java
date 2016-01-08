package com.aetoslabs.quickfacts.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.aetoslabs.quickfacts.AppCompatPreferenceActivity;
import com.aetoslabs.quickfacts.R;
import com.aetoslabs.quickfacts.core.DbOpenHelper;
import com.aetoslabs.quickfacts.core.User;


public class SettingsActivity extends AppCompatPreferenceActivity implements BaseContext {
    public static final String PREF_KEY_INCLUDE_ANON_FACTS = "pref_include_anon_facts";
    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    private final String TAG = SettingsActivity.class.getSimpleName();
    protected SharedPreferences session;
    protected DbOpenHelper mFactDbHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        session = getSharedPreferences(BaseActivity.APP_SESSION, Context.MODE_PRIVATE);

        mFactDbHelper = new DbOpenHelper(this);
        setContentView(R.layout.activity_settings);
        Log.d(TAG, sharedPref.getAll().toString());
    }

    public boolean isLoggedIn() {
        return session.contains(BaseActivity.PARAM_USER_ID) && session.getInt(BaseActivity.PARAM_USER_ID, -1) != -1
                && session.contains(BaseActivity.PARAM_USER_EMAIL) && !session.getString(BaseActivity.PARAM_USER_EMAIL, "").isEmpty();
    }

    public User getCurrentUser() {
        if (!isLoggedIn()) return null;
        return new User().findById(this, session.getInt(BaseActivity.PARAM_USER_ID, -1));
    }

    public SQLiteDatabase getWritableDb() {
        return mFactDbHelper.getWritableDatabase();
    }

    public SQLiteDatabase getReadableDb() {
        return mFactDbHelper.getReadableDatabase();
    }

    public static class AppPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            User currentUser = ((SettingsActivity) getActivity()).getCurrentUser();
            if (currentUser != null) {
                getPreferenceManager().setSharedPreferencesName(currentUser.email);
                getPreferenceManager().setSharedPreferencesMode(Context.MODE_PRIVATE);
            }

            addPreferencesFromResource(R.xml.preferences);
        }
    }
}
