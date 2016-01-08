package com.aetoslabs.quickfacts.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.aetoslabs.quickfacts.BuildConfig;
import com.aetoslabs.quickfacts.R;
import com.aetoslabs.quickfacts.RegistrationIntentService;
import com.aetoslabs.quickfacts.SearchResultsView;
import com.aetoslabs.quickfacts.SyncService;
import com.aetoslabs.quickfacts.core.Fact;
import com.aetoslabs.quickfacts.core.ServerResponse;
import com.aetoslabs.quickfacts.core.User;
import com.aetoslabs.quickfacts.core.Utils;
import com.aetoslabs.quickfacts.fragments.AddFactFragment;
import com.aetoslabs.quickfacts.fragments.SearchResultsFragment;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends BaseActivity implements AddFactFragment.EditNameDialogListener,
        SearchView.OnQueryTextListener {
    public static final String TAG = MainActivity.class.getSimpleName();
    SyncService mService;
    ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ((SyncService.SyncServiceBinder) service).getService();
            Log.d(TAG, "Service connected " + name + " " + service);

            if (BuildConfig.DEBUG) {
                Integer userId = null;
                boolean includeAnon = true;
                if (isLoggedIn()) {
                    userId = session.getInt(PARAM_USER_ID, -1);
                    includeAnon = getPrefs().getBoolean(SettingsActivity.PREF_KEY_INCLUDE_ANON_FACTS, false);
                }
                mService.search("f", userId, includeAnon);
                mProgressDialog.setMessage("Searching Facts...");
                mProgressDialog.show();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            Log.d(TAG, "Service disconnected " + name);
        }
    };

    SearchResultsView mSearchResultsView;
    SearchResultsFragment.SearchResultsAdapter mSearchResultsViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mSearchResultsView = (SearchResultsView) findViewById(R.id.list);
        mSearchResultsViewAdapter = (SearchResultsFragment.SearchResultsAdapter) mSearchResultsView.getAdapter();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SearchView searchView = (SearchView) findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddFactFragment testDialog = new AddFactFragment();
                testDialog.show(getSupportFragmentManager(), "fragment_name");
            }
        });
        Log.d(TAG, "On create..." + savedInstanceState);

        if (BuildConfig.DEBUG) {
            toolbar.setBackgroundResource(android.R.color.holo_orange_dark);
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(SyncService.ACTION_SEARCH_RESULT);
        filter.addAction(SyncService.ACTION_ADD_FACT);
        filter.addAction(RegistrationIntentService.ACTION_REGISTRATION_COMPLETE);
        filter.addAction(LoginActivity.ACTION_USER_LOGGED_IN);
        registerReceiver(receiver, filter);

        // Start IntentService to register this application with GCM.
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
        Log.d(TAG, "onCreate: registered receiver");
    }

    public void onReceiveBroadcast(Context context, Intent intent) {
        Log.d(TAG, "onReceive: " + context + " Int=" + intent);
        String action = intent.getAction();

        if (action.equals(RegistrationIntentService.ACTION_REGISTRATION_COMPLETE)) {
            Log.d(TAG, "onReceive: registration complete " + intent);
            MainActivity.this.mProgressDialog.dismiss();
            return;
        }

        ServerResponse response = (ServerResponse) intent.getSerializableExtra(SyncService.KEY_SERVER_RESPONSE);

        if (response.errors != null && !response.errors.isEmpty()) {
            for (Map.Entry<String, String[]> pair : response.errors.entrySet()) {
                Log.e(TAG, pair.getKey() + ": " + pair.getValue()[0]);
            }
            MainActivity.this.mProgressDialog.dismiss();
            Toast.makeText(this, response.errors.values().iterator().next()[0], Toast.LENGTH_LONG).show();
            return;
        }

        if (action.equals(SyncService.ACTION_SEARCH_RESULT)) {
            mSearchResultsViewAdapter.clear();
            mSearchResultsViewAdapter.addAll(Lists.newArrayList(Iterables.filter(response.facts, new Predicate<Fact>() {
                @Override
                public boolean apply(Fact input) {
                    return !input.isDeleted();
                }
            })));
            mSearchResultsView.scrollTo(0, 0);

        } else if (action.equals(SyncService.ACTION_ADD_FACT)) {
            if (mSearchResultsViewAdapter.getCount() == 1 && mSearchResultsViewAdapter.getItem(0).userId == -1) {
                mSearchResultsViewAdapter.clear();
            }
            mSearchResultsViewAdapter.insert(response.fact, 0);
        } else {
            Log.e(TAG, "onReceive: Unkown action \"" + action + "\"");
        }

        MainActivity.this.mProgressDialog.dismiss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: req_code=" + requestCode + " res_code=" + resultCode);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        Log.d(TAG, "onDestroy: unregistered receiver");
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intentService = new Intent(this, SyncService.class);
        bindService(intentService, mServiceConn, Context.BIND_AUTO_CREATE);
        Log.d(TAG, "Started, service bound...");
    }

    @Override
    protected void onStop() {
        super.onStop();

        unbindService(mServiceConn);
        Log.d(TAG, "Stopped, service unbound...");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (getCurrentUser() != null) {
            menu.findItem(R.id.menu_login).setVisible(false);
            menu.findItem(R.id.menu_create_account).setVisible(false);

            menu.findItem(R.id.menu_logout).setVisible(true);
            menu.findItem(R.id.menu_preferences).setVisible(true);
        }

        if (BuildConfig.DEBUG) {
            menu.findItem(R.id.menu_test_btn).setVisible(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Log.d(TAG, "Menu clicked " + item);
        switch (item.getItemId()) {
            case R.id.menu_login:
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_logout:
                User currentUser = getCurrentUser();
                currentUser.logout(this);
                invalidateOptionsMenu();
                Toast.makeText(this, "Logged out " + currentUser.name, Toast.LENGTH_SHORT).show();
                recreate();
                break;
            case R.id.menu_create_account:
                Intent registerActivity = new Intent(this, RegistrationActivity.class);
                startActivity(registerActivity);
                break;
            case R.id.menu_preferences:
                Log.d(TAG, "launch session activity here");
                Intent settingsActivity = new Intent(this, SettingsActivity.class);
                startActivity(settingsActivity);
                break;
            case R.id.menu_test_btn:
                doTest();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void doTest() {
        //Log.d(TAG, "Unsorted " + (new Fact().getLastUpdated(MainActivity.this)));
        Date date = new Date();
        String fmt = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        String dateFormat = new SimpleDateFormat(Utils.DATE_TIME_FORMAT, Locale.ENGLISH).format(date);
        Log.d(TAG, "Time: " + dateFormat);
        //Log.d(TAG, "Test " + SyncService.ACTION_SEARCH_RESULT);
        //mService.addFact(new Fact("some random fact! " + new Random().nextLong(), session.contains(PARAM_USER_ID) ? session.getInt(PARAM_USER_ID, 0) : null));
        Log.d(TAG, "doTest: done");
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        Log.d("Main", "Query changed: " + newText);
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.d("Main", "Submitted: " + query);
        Integer userId = null;
        boolean includeAnon = true;
        if (isLoggedIn()) {
            userId = session.getInt(PARAM_USER_ID, -1);
            includeAnon = getPrefs().getBoolean(SettingsActivity.PREF_KEY_INCLUDE_ANON_FACTS, false);
        }
        mService.search(query, userId, includeAnon);
        mProgressDialog.setMessage("Searching Facts...");
        mProgressDialog.show();
        return false;
    }

    public void onFinishEditDialog(String txt) {
        Log.d(TAG, "Finished: " + txt);
        if (txt.trim().isEmpty()) return;
        mService.addFact(new Fact(txt, session.contains(PARAM_USER_ID) ? session.getInt(PARAM_USER_ID, 0) : null));
    }

    public static void main(String[] args) {
        System.out.println("Hello world...");
    }
}
