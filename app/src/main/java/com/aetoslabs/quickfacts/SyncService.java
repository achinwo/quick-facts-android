package com.aetoslabs.quickfacts;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.aetoslabs.quickfacts.activities.BaseContext;
import com.aetoslabs.quickfacts.core.DbOpenHelper;
import com.aetoslabs.quickfacts.core.Fact;
import com.aetoslabs.quickfacts.tasks.AddFactTask;
import com.aetoslabs.quickfacts.tasks.DeleteFactTask;
import com.aetoslabs.quickfacts.tasks.SearchTask;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class SyncService extends Service implements BaseContext {

    private static final String TAG = SyncService.class.getSimpleName();
    public static final String ACTION_SEARCH_RESULT = SyncService.class.getCanonicalName() + ".SEARCH_RESULT";
    public static final String KEY_SERVER_RESPONSE = "SEARCH_RESULT";
    public static final String KEY_DELETED_FACT_ID = "DELETED_FACT";
    public static final String ACTION_ADD_FACT = SyncService.class.getCanonicalName() + ".ADD_FACT";
    public static final String ACTION_FACT_DELETED = SyncService.class.getCanonicalName() + ".FACT_DELETED";
    public static final String ACTION_DELETE_FACT = SyncService.class.getCanonicalName() + ".DELETE_FACT";
    private final IBinder mBinder = new SyncServiceBinder();
    private RequestQueue mRequestQueue;
    protected DbOpenHelper mFactDbHelper;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            SyncService.this.onReceive(context, intent);
        }
    };

    private void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: " + intent.getAction() + " context=" + context);
        Fact fact = new Fact().findById(this, intent.getIntExtra(KEY_DELETED_FACT_ID, -1));

        Log.d(TAG, "onReceive: deleting this fact " + fact);
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .encodedAuthority(BuildConfig.SERVER_URL)
                .appendPath("facts")
                .appendPath(String.valueOf(fact.getId()) + ".json");
        String url = builder.build().toString();

        new DeleteFactTask(this, mRequestQueue, ACTION_FACT_DELETED).execute(url);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public SQLiteDatabase getWritableDb() {
        return mFactDbHelper.getWritableDatabase();
    }

    public SQLiteDatabase getReadableDb() {
        return mFactDbHelper.getReadableDatabase();
    }

    public void search(String query, Integer userId, boolean includeAnon) {
        Uri.Builder builder = new Uri.Builder();

        builder.scheme("http")
                .encodedAuthority(BuildConfig.SERVER_URL)
                .appendPath("facts.json")
                .appendQueryParameter("include_anon", String.valueOf(includeAnon))
                .appendQueryParameter("query", query);

        if (userId != null) builder.appendQueryParameter("user_id", String.valueOf(userId));
        String url = builder.build().toString();
        Log.d(TAG, "search: " + url);

        new SearchTask(this, mRequestQueue, ACTION_SEARCH_RESULT).execute(url);
    }

    public void addFact(Fact newFact) {
        Uri.Builder builder = new Uri.Builder();

        builder.scheme("http")
                .encodedAuthority(BuildConfig.SERVER_URL)
                .appendPath("facts.json");

        String url = builder.build().toString();
        Log.d(TAG, "add fact: " + url);

        new AddFactTask(this, mRequestQueue, ACTION_ADD_FACT, newFact).execute(url);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "created service");
        mRequestQueue = Volley.newRequestQueue(this);
        mFactDbHelper = new DbOpenHelper(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_DELETE_FACT);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        Log.d(TAG, "service destroyed");
    }

    public class SyncServiceBinder extends Binder {
        public SyncService getService() {
            return SyncService.this;
        }
    }


}
