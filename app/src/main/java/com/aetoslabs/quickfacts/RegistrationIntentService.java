package com.aetoslabs.quickfacts;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.aetoslabs.quickfacts.activities.SettingsActivity;
import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

public class RegistrationIntentService extends IntentService {

    public static final String ACTION_REGISTRATION_COMPLETE = "registrationComplete";
    private static String TAG = RegistrationIntentService.class.getSimpleName();
    private static final String[] TOPICS = {"global"};


    public RegistrationIntentService() {
        super(TAG);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        try {
            Log.i(TAG, "GCM Generating token...");
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getString(R.string.gcm_project_id),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            Log.i(TAG, "GCM Token: " + token + " END");

            sendRegistrationToServer(token);

            // Subscribe to topic channels
            subscribeTopics(token);
            sharedPreferences.edit().putBoolean(SettingsActivity.SENT_TOKEN_TO_SERVER, true).apply();
        } catch (IOException e) {
            Log.d(TAG, "Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            sharedPreferences.edit().putBoolean(SettingsActivity.SENT_TOKEN_TO_SERVER, false).apply();
        }
        Intent registrationComplete = new Intent(ACTION_REGISTRATION_COMPLETE);
        sendBroadcast(registrationComplete);

    }

    private void subscribeTopics(String token) throws IOException {
        GcmPubSub pubSub = GcmPubSub.getInstance(this);
        for (String topic : TOPICS) {
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }

    private void sendRegistrationToServer(String token) {

    }
}
