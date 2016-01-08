package com.aetoslabs.quickfacts.tasks;

import android.net.Uri;

import com.aetoslabs.quickfacts.BuildConfig;
import com.aetoslabs.quickfacts.activities.BaseContext;
import com.aetoslabs.quickfacts.core.ServerResponse;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

/**
 * Created by anthony.chinwo on 08/01/16.
 */
public class LoginTask extends BaseTask<String, Void> {
    private static String TAG = LoginTask.class.getSimpleName();
    String mEmail, mPassword;

    public LoginTask(BaseContext context, RequestQueue queue, String action, String email, String password) {
        super(context, queue, action);
        mEmail = email;
        mPassword = password;
    }

    @Override
    public ServerResponse onDoInBackground(String... strings) throws Exception {
        int REQUEST_TIMEOUT = 10;
        RequestFuture<JSONObject> future = RequestFuture.newFuture();

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .encodedAuthority(BuildConfig.SERVER_URL)
                .appendPath("authenticate.json");

        String url = builder.build().toString();

        JSONObject cred = new JSONObject("{\"email\":\"" + mEmail + "\", \"password\":\"" + mPassword + "\"}");
        JsonObjectRequest request = new JsonObjectRequest(url, cred, future, future);
        mRequestQueue.add(request);
        JSONObject response = future.get(REQUEST_TIMEOUT_SECS, TimeUnit.SECONDS); // this will block (forever)
        ServerResponse serverResponse = new Gson().fromJson(response.toString(), ServerResponse.class);

        if (serverResponse != null && serverResponse.user != null) {
            serverResponse.user.write(mContext);
        }

        return serverResponse;
    }
}
