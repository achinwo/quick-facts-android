package com.aetoslabs.quickfacts.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ScrollView;
import android.widget.TextView;

import com.aetoslabs.quickfacts.DeleteTouchListener;
import com.aetoslabs.quickfacts.R;
import com.aetoslabs.quickfacts.SearchResultsView;
import com.aetoslabs.quickfacts.activities.BaseActivity;
import com.aetoslabs.quickfacts.activities.MainActivity;
import com.aetoslabs.quickfacts.core.Fact;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;



public class SearchResultsFragment extends Fragment {

    private static final String TAG = SearchResultsFragment.class.getSimpleName();

    DataSetObserver mObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            onArrayChanged();
        }
    };

    SharedPreferences session;
    SearchResultsView mListView;
    SearchResultsAdapter mAdapter;
    ScrollView mResultsScrollView;
    ArrayList<Fact> results;
    private TextView mNoResultTv;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);

        Log.d(TAG, "onCreate() " + view.getClass().getName());
        mResultsScrollView = (ScrollView) view.findViewById(R.id.search_results_scroll_view);
        mNoResultTv = (TextView) view.findViewById(R.id.no_results);
        session = getActivity().getSharedPreferences(MainActivity.APP_SESSION, Context.MODE_PRIVATE);
        Context context = view.getContext();
        mListView = (SearchResultsView) view.findViewById(R.id.list);
        results = new ArrayList<>();
        mAdapter = new SearchResultsAdapter(context, results);
        mListView.setAdapter(mAdapter);
        mAdapter.registerDataSetObserver(mObserver);
        return view;
    }

    public BaseActivity getBaseActivity() {
        return (BaseActivity) getActivity();
    }

    public void onArrayChanged() {
        final boolean hideResultView = mAdapter.getCount() == 0;

        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mResultsScrollView.setVisibility(hideResultView ? View.GONE : View.VISIBLE);
        mResultsScrollView.animate().setDuration(shortAnimTime).alpha(
                hideResultView ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mResultsScrollView.setVisibility(hideResultView ? View.GONE : View.VISIBLE);
            }
        });

        mNoResultTv.setVisibility(hideResultView ? View.VISIBLE : View.GONE);
        mNoResultTv.animate().setDuration(shortAnimTime).alpha(
                hideResultView ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mNoResultTv.setVisibility(hideResultView ? View.VISIBLE : View.GONE);
            }
        });
    }

    public class SearchResultsAdapter extends ArrayAdapter<Fact> {

        public SearchResultsAdapter(Context context, ArrayList<Fact> results) {
            super(context, 0, results);
        }

        @Override
        public View getView(int position, View convertView, final ViewGroup parent) {
            Fact result = getItem(position);
            Log.d(TAG, "position is " + position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.search_result, parent, false);
            }

            convertView.setTag(result);

            View view = convertView;
            view.setClickable(true);
            view.setFocusable(true);

            DeleteTouchListener.DeleteCallback callback = new DeleteTouchListener.DeleteCallback() {
                @Override
                public void onDelete(View view) {
                    Log.d(TAG, "onDelete: deleting " + view);
                    view.setOnTouchListener(null);

                    Fact fact = (Fact) view.getTag();
                    fact.delete((BaseActivity) getActivity());
                    mAdapter.remove(fact);
                }
            };

            view.setOnTouchListener(new DeleteTouchListener(view.findViewById(R.id.search_result_body), callback));

            TextView tv = (TextView) convertView.findViewById(R.id.content);
            tv.setText(result.getContent());
            tv.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    ActionBar actionBar = ((MainActivity) SearchResultsFragment.this.getActivity()).getSupportActionBar();
                    if (actionBar != null) {
                        actionBar.setShowHideAnimationEnabled(true);
                        actionBar.hide();
                    }
                    Log.d(TAG, "Hiding action bar");
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    return false;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    final ActionBar actionBar = ((MainActivity) SearchResultsFragment.this.getActivity()).getSupportActionBar();
                    if (actionBar != null) actionBar.show();
                    Log.d(TAG, "showing action bar");
                }
            });


            if (result.userId != null && result.userId > 0) {
                String userName = session.getInt(MainActivity.PARAM_USER_ID, -1) == result.userId ? "" : result.userId.toString();
                ((TextView) convertView.findViewById(R.id.result_user_name)).setText(userName);
            }

            Date updatedAt = result.getCreatedAt();
            ((TextView) convertView.findViewById(R.id.result_updated_at)).setText(
                    updatedAt == null ? "" : DateFormat.getDateInstance().format(updatedAt));

            return convertView;
        }


    }

}
