package com.aetoslabs.quickfacts;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

/**
 * Created by anthony on 19/12/15.
 */
public class RegisterationActivity extends AppCompatActivity {
    public static final String TAG = RegisterationActivity.class.getSimpleName();
    RequestQueue queue;
    EditText mPassword, mPasswordConfirmation, mName, mEmail;

    public RegisterationActivity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        queue = Volley.newRequestQueue(this);
        setContentView(R.layout.activity_registeration);

        mName = (EditText) findViewById(R.id.register_full_name);
        mPassword = (EditText) findViewById(R.id.register_password);
        mPasswordConfirmation = (EditText) findViewById(R.id.register_password_confirmation);
        mEmail = (EditText) findViewById(R.id.register_email);
    }

    public void createAccount(View button) {
        String registerationUrl = MainActivity.SERVER_URL + "/users.json";
        User newUser = new User();
        newUser.email = mEmail.getText().toString();
        newUser.name = mName.getText().toString();
        newUser.password = mPassword.getText().toString();

        String newUserJson = new Gson().toJson(newUser);
        Log.d(TAG, "New User " + newUserJson);
    }

    public void login(View backToLogin) {
        finish();
    }

}
