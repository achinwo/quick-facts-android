package com.aetoslabs.quickfacts;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by anthony on 02/01/16 as part of Quick Facts.
 */
public class AppInstanceIDListenerService extends InstanceIDListenerService {
    private static String TAG = AppInstanceIDListenerService.class.getSimpleName();

    @Override
    public void onTokenRefresh() {
        Log.d(TAG, "onTokenRefresh: refreshing GCM instance token");
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
    }

}
