package com.aetoslabs.quickfacts.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.aetoslabs.quickfacts.BuildConfig;
import com.aetoslabs.quickfacts.R;
import com.aetoslabs.quickfacts.core.User;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Created by anthony on 19/12/15.
 */
public class RegistrationActivity extends BaseActivity {
    public static final String TAG = RegistrationActivity.class.getSimpleName();
    EditText mPasswordView, mPasswordConfirmationView, mNameView, mEmailView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mNameView = (EditText) findViewById(R.id.register_full_name);
        mPasswordView = (EditText) findViewById(R.id.register_password);
        mPasswordConfirmationView = (EditText) findViewById(R.id.register_password_confirmation);
        mEmailView = (EditText) findViewById(R.id.register_email);

        mLoginFormView = findViewById(R.id.register_form);
        mProgressView = findViewById(R.id.register_progress);

        if (BuildConfig.BUILD_TYPE.equals("debug")) {
            mNameView.setText("Tester1");
            mEmailView.setText("tester@tested1.com");
            mPasswordView.setText("testing");
        }
    }

    public void createAccount(View button) {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mNameView.setError(null);

        if (!mPasswordView.getText().toString().equals(mPasswordConfirmationView.getText().toString())) {
            mPasswordConfirmationView.setError("doesn't match password");
            return;
        } else {
            mPasswordConfirmationView.setError(null);
        }

        showProgress(true);

        String registrationUrl = BuildConfig.SERVER_URL + "/users.json";
        User newUser = new User();
        newUser.email = mEmailView.getText().toString();
        newUser.name = mNameView.getText().toString();
        newUser.password = mPasswordView.getText().toString();

        final Gson gson = new Gson();
        final String newUserJson = gson.toJson(newUser);
        Log.d(TAG, "New User " + newUserJson);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, registrationUrl,
                "{\"user\":" + newUserJson + "}",
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        showProgress(false);
                        User createdUser = new Gson().fromJson(response.toString(), User.class);
                        Log.d(TAG, createdUser.name + "  " + createdUser.email + "  " + createdUser.id);
                        Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        Bundle extras = new Bundle();
                        extras.putSerializable(MainActivity.PARAM_USER, createdUser);
                        intent.putExtras(extras);
                        startActivity(intent);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showProgress(false);

                Toast.makeText(RegistrationActivity.this,
                        (error instanceof TimeoutError) ? "Server timedout, please try again momentarily"
                                : "Error: " + error.toString(),
                        Toast.LENGTH_LONG).show();
                String err = error.toString();
                if (error.networkResponse != null && error.networkResponse.data != null) {
                    String errorJson = new String(error.networkResponse.data);
                    Type type = new TypeToken<Map<String, Map<String, String[]>>>() {
                    }.getType();
                    Map<String, Map<String, String[]>> errorMap = gson.fromJson(errorJson, type);

                    Log.d(TAG, errorMap.toString());

                    if (errorMap.containsKey("errors") && errorMap.get("errors").get("email") != null) {
                        mEmailView.setError(errorMap.get("errors").get("email")[0]);
                    }

                    if (errorMap.containsKey("errors") && errorMap.get("errors").get("password") != null) {
                        mPasswordView.setError(errorMap.get("errors").get("password")[0]);
                    }

                    if (errorMap.containsKey("errors") && errorMap.get("errors").get("name") != null) {
                        mNameView.setError(errorMap.get("errors").get("name")[0]);
                    }
                }
                Log.e(TAG, "Registration error: " + err);
            }
        });

        queue.add(request);
    }

    public void login(View backToLogin) {
        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivity(loginIntent);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

}
