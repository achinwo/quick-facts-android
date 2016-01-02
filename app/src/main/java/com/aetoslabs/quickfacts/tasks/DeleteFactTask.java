package com.aetoslabs.quickfacts.tasks;

import android.util.Log;

import com.aetoslabs.quickfacts.activities.BaseContext;
import com.aetoslabs.quickfacts.core.ServerResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

/**
 * Created by anthony on 02/01/16.
 */
public class DeleteFactTask extends BaseTask<String, Void> {

    private static String TAG = DeleteFactTask.class.getSimpleName();

    public DeleteFactTask(BaseContext context, RequestQueue queue, String action) {
        super(context, queue, action);
    }

    @Override
    public ServerResponse onDoInBackground(String... params) throws Exception {
        String url = params[0];
        int REQUEST_TIMEOUT = 10;
        RequestFuture<JSONObject> future = RequestFuture.newFuture();

        Gson gson = new Gson();
        mRequestQueue.add(new JsonObjectRequest(Request.Method.POST, url, future, future));
        JSONObject response = future.get(REQUEST_TIMEOUT, TimeUnit.SECONDS);
        ServerResponse serverResponse = new Gson().fromJson(response.toString(), ServerResponse.class);
        //serverResponse.fact.write(mContext);
        Log.d(TAG, "doInBackground: " + serverResponse.fact);

        return serverResponse;
    }
}
