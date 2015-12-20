package com.aetoslabs.quickfacts;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements SearchResultsFragment.OnListFragmentInteractionListener,
                   AddFactFragment.OnFragmentInteractionListener,
                    AddFactFragment.EditNameDialogListener,
                   SearchView.OnQueryTextListener {
    public static final String TAG = MainActivity.class.getSimpleName();
    public static final String APP_SESSION = "QuickFactSession";
    //public static final String APP_PREFERENCES = "QuickFactPrefs";
    public static final String PARAM_USER_NAME = "USER_NAME";
    public static final String PARAM_USER_EMAIL = "USER_EMAIL";
    public static final String PARAM_USER_ID = "USER_ID";
    public static final String PARAM_USER = "USER_OBJ";

    private ProgressDialog progressDialog;

    SharedPreferences session, prefs;

    protected RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressDialog = new ProgressDialog(this);
        queue = Volley.newRequestQueue(this);
        session = getSharedPreferences(APP_SESSION, Context.MODE_PRIVATE);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.activity_main);
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

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.getSerializable(PARAM_USER) != null) {
            User user = (User) extras.getSerializable(PARAM_USER);
            Log.d(TAG, "Extra " + user);
            SharedPreferences.Editor editor = session.edit();
            editor.putString(MainActivity.PARAM_USER_NAME, user.name);
            editor.putString(MainActivity.PARAM_USER_EMAIL, user.email);
            editor.putInt(MainActivity.PARAM_USER_ID, user.id);
            editor.commit();
            Toast.makeText(this, "Logged in " + user.name, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "resumed...");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "paused...");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (session.contains(PARAM_USER_ID)) {
            menu.findItem(R.id.menu_login).setVisible(false);
            menu.findItem(R.id.menu_create_account).setVisible(false);

            menu.findItem(R.id.menu_logout).setVisible(true);
            menu.findItem(R.id.menu_preferences).setVisible(true);
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
                SharedPreferences.Editor editor = session.edit();
                String userName = session.getString(PARAM_USER_NAME, "");
                editor.clear();
                editor.commit();
                invalidateOptionsMenu();
                Toast.makeText(this, "Logged out " + userName, Toast.LENGTH_SHORT).show();
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
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onListFragmentInteraction(Object item) {
        Log.d("Fragg", "hello frag");
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        Log.d("Main", "Query changed: " + newText);
        return false;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        Log.d("Main", "Fragment: " + uri.toString());
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.d("Main", "Submitted: " + query);
        search(query);
        return false;
    }

    public void onFinishEditDialog(String txt){
        Log.d(TAG, "Finished: " + txt);
        if (txt.trim().isEmpty()) return;
        addFact(new Fact(txt, session.contains(PARAM_USER_ID) ? session.getInt(PARAM_USER_ID, 0) : null));
    }

    public SearchResultsFragment.SearchResultsAdapter getSearchResultsAdapter(){
        ListView view = (ListView) findViewById(R.id.search_result_item_fragment);
        return (SearchResultsFragment.SearchResultsAdapter) view.getAdapter();
    }

    public ListView getSearchResultsListView(){
        return (ListView) findViewById(R.id.search_result_item_fragment);
    }

    public void addFact(Fact fact){
        String url = BuildConfig.SERVER_URL + "/facts.json";
        final Gson gson = new Gson();
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, gson.toJson(fact),
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {
                        SearchResultsFragment.SearchResultsAdapter adapter = getSearchResultsAdapter();

                        if (adapter.getCount() == 1 && adapter.getItem(0).userId == -1) {
                            adapter.clear();
                        }
                        adapter.insert(gson.fromJson(response.toString(), Fact.class), 0);
                        getSearchResultsListView().smoothScrollToPosition(0);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this,
                                (error instanceof TimeoutError) ? "Server timedout, please try again momentarily"
                                        : "Error: " + error.toString(),
                                Toast.LENGTH_LONG).show();
                    }
                }
        );
        queue.add(jsonRequest);
    }

    public void search(String query){
        String url = BuildConfig.SERVER_URL + "/facts.json?query=" + query;

        if (session.contains(PARAM_USER_ID)) {
            url += "&user_id=" + session.getInt(PARAM_USER_ID, -1);
            url += "&include_anon=" + prefs.getBoolean(SettingsActivity.PREF_KEY_INCLUDE_ANON_FACTS, false);
        }

        Log.d(TAG, "URL=" + url);
        final JsonArrayRequest jsonRequest = new JsonArrayRequest(Request.Method.GET, url,
                new Response.Listener<JSONArray>(){
                    @Override
                    public void onResponse(JSONArray response) {
                        SearchResultsFragment.SearchResultsAdapter adapter = getSearchResultsAdapter();
                        adapter.clear();
                        Gson gson = new Gson();
                        ArrayList<Fact> facts = gson.fromJson(response.toString(), (new TypeToken<ArrayList<Fact>>() {}).getType());
                        adapter.addAll(facts);
                        MainActivity.this.progressDialog.dismiss();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this,
                                (error instanceof TimeoutError) ? "Server timedout, please try again momentarily"
                                        : "Error: " + error.toString(),
                                Toast.LENGTH_LONG).show();
                        MainActivity.this.progressDialog.dismiss();
                    }
                }
        );

        progressDialog.setMessage("Searching facts...");
        progressDialog.show();
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface arg0) {
                jsonRequest.cancel();
                Log.d(TAG, "search cancelled");
            }
        });
        queue.add(jsonRequest);
    }

    public static void main(String[] args) {
        System.out.println("Hello world...");
    }
}
