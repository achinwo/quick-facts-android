package com.aetoslabs.quickfacts.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.aetoslabs.quickfacts.BuildConfig;
import com.aetoslabs.quickfacts.R;
import com.aetoslabs.quickfacts.SyncService;
import com.aetoslabs.quickfacts.core.ServerResponse;
import com.aetoslabs.quickfacts.core.User;
import com.aetoslabs.quickfacts.tasks.RegistrationTask;

import java.util.HashMap;

/**
 * Created by anthony on 19/12/15.
 */
public class RegistrationActivity extends BaseActivity {
    public static final String TAG = RegistrationActivity.class.getSimpleName();
    public static String ACTION_USER_REGISTRATION = "userRegisteration";
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

        if (BuildConfig.DEBUG) {
            mNameView.setText("Tester1");
            mEmailView.setText("tester@tested1.com");
            mPasswordView.setText("testing");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(receiver, new IntentFilter(ACTION_USER_REGISTRATION));
        Log.d(TAG, "onStart: registered receiver");
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(receiver);
        Log.d(TAG, "onStop: unregistered receiver");
    }

    public void onReceiveBroadcast(Context context, Intent intent) {
        Log.d(TAG, "onReceiveBroadcast: " + context);
        showProgress(false);

        ServerResponse response = (ServerResponse) intent.getSerializableExtra(SyncService.KEY_SERVER_RESPONSE);
        if (response.errors != null && !response.errors.isEmpty()) {

            HashMap<String, String[]> errorMap = response.errors;
            Log.d(TAG, errorMap.toString());

            if (errorMap.containsKey("email")) {
                mEmailView.setError(errorMap.get("email")[0]);
            }

            if (errorMap.containsKey("password")) {
                mPasswordView.setError(errorMap.get("password")[0]);
            }

            if (errorMap.containsKey("name")) {
                mNameView.setError(errorMap.get("name")[0]);
            }

            if (errorMap.containsKey("ExecutionException")) {
                Toast.makeText(this, errorMap.get("ExecutionException")[0], Toast.LENGTH_LONG).show();
            }

        } else {
            if (response.user.login(this)) {
                Toast.makeText(this, "Logged in " + response.user.name, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Logged in failed!", Toast.LENGTH_SHORT).show();
            }
            Intent mainActivityIntent = new Intent(RegistrationActivity.this, MainActivity.class);
            mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(mainActivityIntent);
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

        Uri.Builder builder = new Uri.Builder();


        String registrationUrl = builder.scheme("http")
                .encodedAuthority(BuildConfig.SERVER_URL)
                .appendPath("users.json").build().toString();
        User newUser = new User();
        newUser.email = mEmailView.getText().toString();
        newUser.name = mNameView.getText().toString();
        newUser.password = mPasswordView.getText().toString();

        new RegistrationTask(this, queue, ACTION_USER_REGISTRATION, newUser).execute(registrationUrl);
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
