package com.aetoslabs.quickfacts;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

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

    public int getRandomNumber() {
        return mGenerator.nextInt(100);
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
}
