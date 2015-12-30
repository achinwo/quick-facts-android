package com.aetoslabs.quickfacts.fragments;

import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.aetoslabs.quickfacts.R;


public class AddFactFragment extends AppCompatDialogFragment implements TextView.OnEditorActionListener {
    public static final String TAG = AddFactFragment.class.getSimpleName();
    private EditText mEditText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_fact, container, false);
        mEditText = (EditText) view.findViewById(R.id.editText);

        view.findViewById(R.id.button2).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AddFactFragment.this.dismiss();
                    }
                }
        );

        view.findViewById(R.id.button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AddFactFragment.this.doAddFact();
                    }
                }
        );

        getDialog().setTitle("Enter new fact");

        // Show soft keyboard automatically
        mEditText.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        mEditText.setOnEditorActionListener(this);
        return view;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        Log.d(TAG, "On editor: " + v.getText() + event.toString());
        if (actionId == EditorInfo.IME_NULL
                && event.getAction() == KeyEvent.ACTION_DOWN)  {
            // Return input text to activity®
            doAddFact();
            return true;
        }
        return false;
    }

    public void doAddFact(){
        EditNameDialogListener activity = (EditNameDialogListener) getActivity();
        activity.onFinishEditDialog(mEditText.getText().toString());
        this.dismiss();
    }

    public interface EditNameDialogListener {
        void onFinishEditDialog(String inputText);
    }
}
