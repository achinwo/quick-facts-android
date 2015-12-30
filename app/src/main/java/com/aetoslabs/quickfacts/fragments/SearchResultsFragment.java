package com.aetoslabs.quickfacts.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.aetoslabs.quickfacts.DeleteTouchListener;
import com.aetoslabs.quickfacts.R;
import com.aetoslabs.quickfacts.SearchResultsView;
import com.aetoslabs.quickfacts.activities.BaseActivity;
import com.aetoslabs.quickfacts.activities.MainActivity;
import com.aetoslabs.quickfacts.core.Fact;
import com.aetoslabs.quickfacts.core.Utils;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;


public class SearchResultsFragment extends Fragment {

    private static final String TAG = SearchResultsFragment.class.getSimpleName();
    SharedPreferences session;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);
        Log.d(TAG, "onCreate() " + view.getClass().getName());
        session = getActivity().getSharedPreferences(MainActivity.APP_SESSION, Context.MODE_PRIVATE);
        Context context = view.getContext();
        SearchResultsView listView = (SearchResultsView) view.findViewById(R.id.list);
        ArrayList<Fact> results = new ArrayList<>();
        results.add(new Fact("No search results...", -1));
        SearchResultsAdapter adapter = new SearchResultsAdapter(context, results);
        listView.setAdapter(adapter);

        return view;
    }

    public class SearchResultsAdapter extends ArrayAdapter<Fact> {

        public SearchResultsAdapter(Context context, ArrayList<Fact> results) {
            super(context, 0, results);
        }

        @Override
        public View getView(int position, View convertView, final ViewGroup parent) {
            Fact result = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.search_result, parent, false);
            }

            convertView.setTag(result);

            View view = convertView;
            final Context context = view.getContext();
            view.setClickable(true);
            view.setFocusable(true);

            DeleteTouchListener.DeleteCallback callback = new DeleteTouchListener.DeleteCallback() {
                @Override
                public void onDelete(final View view) {
                    view.setOnTouchListener(null);
                    Animation fadeOutAnimation = AnimationUtils.loadAnimation(context.getApplicationContext(), R.anim.fade_out);
                    fadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationEnd(Animation animation) {

                            new Handler().post(new Runnable() {
                                @Override
                                public void run() {
                                    Fact fact = (Fact) view.getTag();
                                    SearchResultsAdapter.this.remove(fact);
                                    fact.delete((BaseActivity) getActivity());
                                }
                            });
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }

                        @Override
                        public void onAnimationStart(Animation animation) {
                        }
                    });

                    view.setVisibility(View.VISIBLE);
                    view.startAnimation(fadeOutAnimation);
                    view.setVisibility(View.INVISIBLE);
                }
            };

            view.setOnTouchListener(new DeleteTouchListener(view.findViewById(R.id.search_result_body), callback));

            TextView tv = (TextView) convertView.findViewById(R.id.content);
            tv.setText(result.content);
            tv.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    ActionBar actionBar = ((MainActivity) SearchResultsFragment.this.getActivity()).getSupportActionBar();
                    if (actionBar != null) {
                        actionBar.setShowHideAnimationEnabled(true);
                        actionBar.hide();
                    }
                    MenuItem edit = menu.add("Edit");
                    edit.setIcon(android.R.drawable.ic_menu_edit);
                    edit.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                    Log.d(TAG, "Hiding action bar");
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    if (item.getTitle().equals("Edit")) {
                        EditText factTv = (EditText) getActivity().getCurrentFocus();
                        factTv.setEditableFactory(Editable.Factory.getInstance());
                        return true;
                    }
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

            Date updatedAt = Utils.parseDate(result.updatedAt);
            ((TextView) convertView.findViewById(R.id.result_updated_at)).setText(
                    updatedAt == null ? "" : DateFormat.getDateInstance().format(updatedAt));

            return convertView;
        }
    }
}
