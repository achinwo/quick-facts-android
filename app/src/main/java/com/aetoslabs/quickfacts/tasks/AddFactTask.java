package com.aetoslabs.quickfacts.tasks;

import com.aetoslabs.quickfacts.activities.BaseContext;
import com.aetoslabs.quickfacts.core.Fact;
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
public class AddFactTask extends BaseTask<String, Void> {

    private static String TAG = SearchTask.class.getSimpleName();
    private final Fact mFact;

    public AddFactTask(BaseContext context, RequestQueue queue, String action, Fact newFact) {
        super(context, queue, action);
        mFact = newFact;
    }

    @Override
    public ServerResponse onDoInBackground(String... params) throws Exception {
        String url = params[0];
        RequestFuture<JSONObject> future = RequestFuture.newFuture();

        Gson gson = new Gson();
        mRequestQueue.add(new JsonObjectRequest(Request.Method.POST, url, gson.toJson(mFact), future, future));
        JSONObject response = future.get(REQUEST_TIMEOUT_SECS, TimeUnit.SECONDS);
        ServerResponse serverResponse = new Gson().fromJson(response.toString(), ServerResponse.class);
        serverResponse.fact.write(mContext);

        return serverResponse;
    }
}
