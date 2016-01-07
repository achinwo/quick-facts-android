package com.aetoslabs.quickfacts.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.aetoslabs.quickfacts.AppCompatPreferenceActivity;
import com.aetoslabs.quickfacts.R;


public class SettingsActivity extends AppCompatPreferenceActivity {
    public static final String PREF_KEY_INCLUDE_ANON_FACTS = "pref_include_anon_facts";
    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    private final String TAG = SettingsActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        Log.d(TAG, sharedPref.getAll().toString());
    }

    public static class MyPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }
    }
}
