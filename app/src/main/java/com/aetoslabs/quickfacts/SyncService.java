package com.aetoslabs.quickfacts;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Random;

public class SyncService extends Service {

    private static final String TAG = SyncService.class.getSimpleName();
    // Binder given to clients
    private final IBinder mBinder = new SyncServiceBinder();
    // Random number generator
    private final Random mGenerator = new Random();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void syncFacts(Bundle args, ResultReceiver callback) {

        Bundle bundle = new Bundle();
        bundle.putString("start", "Timer Started....");

        callback.send(Activity.RESULT_OK, bundle);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "created service");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "service destroyed");
    }

    public class SyncServiceBinder extends Binder {
        public SyncService getService() {
            // Return this instance of LocalService so clients can call public methods
            return SyncService.this;
        }
    }

    protected class SyncTask extends AsyncTask<HashMap<String, String>, Void, JSONObject> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(HashMap<String, String>... params) {

            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);

        }
    }
}
