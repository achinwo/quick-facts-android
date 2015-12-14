package com.aetoslabs.quickfacts;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SearchResultsFragment.OnListFragmentInteractionListener, SearchView.OnQueryTextListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SearchView searchView = (SearchView) findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public void onListFragmentInteraction(Object item) {

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.d("Main", "Submitted: "+query);
        URL requestUrl = null;

        try {
            requestUrl = new URL("https://quick-facts.herokuapp.com/facts.json?q=" + query);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        new ResultDownloader(this).execute(requestUrl);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        Log.d("Main", "Query changed: "+newText);
        return false;
    }

    public class ResultDownloader extends AsyncTask<URL, Void, Void> {

        private ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        InputStream inputStream = null;
        String result = "";
        Activity mContext;

        public ResultDownloader(Activity mContext){
            super();
            this.mContext = mContext;
        }

        protected void onPreExecute() {
            progressDialog.setMessage("Downloading your data...");
            progressDialog.show();
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface arg0) {
                    ResultDownloader.this.cancel(true);
                }
            });
        }

        @Override
        protected Void doInBackground(URL... params) {
            ArrayList<NameValuePair> param = new ArrayList<NameValuePair>();
            String url_select = params[0].toString();
            Log.d("Main", "URL= "+url_select);
            try {
                // HttpClient is more then less deprecated. Need to change to URLConnection
                HttpClient httpClient = new DefaultHttpClient();

                HttpGet httpGet = new HttpGet(url_select);
                //httpGet.setEntity(new UrlEncodedFormEntity(param));
                HttpResponse httpResponse = httpClient.execute(httpGet);
                HttpEntity httpEntity = httpResponse.getEntity();

                // Read content & Log
                inputStream = httpEntity.getContent();
            } catch (UnsupportedEncodingException e1) {
                Log.e("UnsupportedEncodingExp", e1.toString());
                e1.printStackTrace();
            } catch (ClientProtocolException e2) {
                Log.e("ClientProtocolException", e2.toString());
                e2.printStackTrace();
            } catch (IllegalStateException e3) {
                Log.e("IllegalStateException", e3.toString());
                e3.printStackTrace();
            } catch (IOException e4) {
                Log.e("IOException", e4.toString());
                e4.printStackTrace();
            }
            // Convert response to string using String Builder
            try {
                BufferedReader bReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"), 8);
                StringBuilder sBuilder = new StringBuilder();

                String line = null;
                while ((line = bReader.readLine()) != null) {
                    sBuilder.append(line + "\n");
                }

                inputStream.close();
                result = sBuilder.toString();

            } catch (Exception e) {
                Log.e("StringBuilding", "Error converting result " + e.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            try {
                JSONArray jArray = new JSONArray(result);
                ArrayList<SearchResult> res = new ArrayList<SearchResult>();
                for(int i=0; i < jArray.length(); i++) {

                    JSONObject jObject = jArray.getJSONObject(i);

                    String content = jObject.getString("content");
                    String id = jObject.getString("id");
                    res.add(new SearchResult(content, id));

                } // End Loop
                this.progressDialog.dismiss();
                Log.d("Main", "Results: " + res.toString());
                ListView view = (ListView) mContext.findViewById(R.id.search_result_item_fragment);
                SearchResultsFragment.SearchResultsAdapter adapter = (SearchResultsFragment.SearchResultsAdapter) view.getAdapter();
                adapter.clear();
                adapter.addAll(res);
            } catch (JSONException e) {
                Log.e("JSONException", "Error: " + e.toString());
            } // catch (JSONException e)
        } // protected void onPostExecute(Void v)

    }
}
