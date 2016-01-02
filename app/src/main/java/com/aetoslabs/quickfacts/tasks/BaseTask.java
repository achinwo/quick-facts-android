package com.aetoslabs.quickfacts.tasks;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.aetoslabs.quickfacts.SyncService;
import com.aetoslabs.quickfacts.activities.BaseContext;
import com.aetoslabs.quickfacts.core.ServerResponse;
import com.android.volley.RequestQueue;

/**
 * Created by anthony on 02/01/16.
 */
public abstract class BaseTask<Param, Progress> extends AsyncTask<Param, Progress, ServerResponse> {
    private static String TAG = BaseTask.class.getSimpleName();

    BaseContext mContext;
    RequestQueue mRequestQueue;
    String mAction;

    public BaseTask(BaseContext context, RequestQueue queue, String action) {
        mAction = action;
        mRequestQueue = queue;
        mContext = context;
    }

    public abstract ServerResponse onDoInBackground(Param... params) throws Exception;

    @Override
    protected ServerResponse doInBackground(Param... params) {
        ServerResponse serverResponse;
        try {
            serverResponse = onDoInBackground(params);
        } catch (Exception e) {
            Log.e(TAG, "doInBackground: ", e);
            serverResponse = new ServerResponse();
            serverResponse.addError(e);
        }

        return serverResponse;
    }

    @Override
    protected void onPostExecute(ServerResponse serverResponse) {
        super.onPostExecute(serverResponse);
        Intent i = new Intent();
        i.setAction(mAction);
        Bundle extras = new Bundle();
        extras.putSerializable(SyncService.KEY_SERVER_RESPONSE, serverResponse);
        i.putExtras(extras);
        mContext.sendBroadcast(i);
    }
}
