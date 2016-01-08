package com.aetoslabs.quickfacts.tasks;

import android.util.Log;

import com.aetoslabs.quickfacts.activities.BaseContext;
import com.aetoslabs.quickfacts.core.ServerResponse;
import com.aetoslabs.quickfacts.core.User;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

/**
 * Created by anthony.chinwo on 07/01/16.
 */
public class RegistrationTask extends BaseTask<String, Void> {

    private static String TAG = RegistrationTask.class.getSimpleName();

    private final User mUser;

    public RegistrationTask(BaseContext context, RequestQueue queue, String action, User user) {
        super(context, queue, action);
        mUser = user;
    }

    @Override
    public ServerResponse onDoInBackground(String... strings) throws Exception {
        String registrationUrl = strings[0];
        final Gson gson = new Gson();
        final String newUserJson = gson.toJson(mUser);
        RequestFuture<JSONObject> future = RequestFuture.newFuture();


        Log.d(TAG, "New User " + newUserJson);
        mRequestQueue.add(new JsonObjectRequest(Request.Method.POST, registrationUrl,
                "{\"user\":" + newUserJson + "}", future, future));
        ServerResponse serverResponse = gson.fromJson(future.get(REQUEST_TIMEOUT_SECS, TimeUnit.SECONDS).toString(), ServerResponse.class);

        if (serverResponse != null && serverResponse.user != null) {
            serverResponse.user.write(mContext);
        }

        return serverResponse;
    }
}
