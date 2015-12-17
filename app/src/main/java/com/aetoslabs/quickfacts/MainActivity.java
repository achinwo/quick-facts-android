package com.aetoslabs.quickfacts;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
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
    public static final String SERVER_URL = "https://quick-facts.herokuapp.com";
    private ProgressDialog progressDialog;

    protected RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressDialog = new ProgressDialog(this);
        queue = Volley.newRequestQueue(this);
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
                testDialog.setRetainInstance(true);
                testDialog.show(getSupportFragmentManager(), "fragment_name");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    public void login(MenuItem loginButton){
        Log.d(TAG, "Login " + loginButton);
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
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
        Log.d("Main", "Fragment: "+uri.toString());
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
        addFact(new Fact(txt));
    }

    public SearchResultsFragment.SearchResultsAdapter getSearchResultsAdapter(){
        ListView view = (ListView) findViewById(R.id.search_result_item_fragment);
        return (SearchResultsFragment.SearchResultsAdapter) view.getAdapter();
    }

    public ListView getSearchResultsListView(){
        return (ListView) findViewById(R.id.search_result_item_fragment);
    }

    public void addFact(Fact fact){
        String url = SERVER_URL+"/facts.json";
        final Gson gson = new Gson();
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, gson.toJson(fact),
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {
                        SearchResultsFragment.SearchResultsAdapter adapter = getSearchResultsAdapter();

                        if (adapter.getCount() == 1 && adapter.getItem(0).id.equals("-1")){
                            adapter.clear();
                        }
                        adapter.insert(gson.fromJson(response.toString(), Fact.class), 0);
                        getSearchResultsListView().smoothScrollToPosition(0);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        SearchResultsFragment.SearchResultsAdapter adapter = getSearchResultsAdapter();
                        adapter.clear();
                        adapter.add(new Fact("Error adding fact" + error, "eh?"));
                    }
                }
        );
        queue.add(jsonRequest);
    }

    public void search(String query){
        String url = SERVER_URL+"/facts.json?query="+query;
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
                        SearchResultsFragment.SearchResultsAdapter adapter = getSearchResultsAdapter();
                        adapter.clear();
                        adapter.add(new Fact("That thing no work o" + error, "eh?"));
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

}
