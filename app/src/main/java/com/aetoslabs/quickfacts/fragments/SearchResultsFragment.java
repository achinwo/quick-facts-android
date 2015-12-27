package com.aetoslabs.quickfacts.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.ListViewCompat;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.aetoslabs.quickfacts.R;
import com.aetoslabs.quickfacts.activities.MainActivity;
import com.aetoslabs.quickfacts.core.Fact;
import com.aetoslabs.quickfacts.core.Utils;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class SearchResultsFragment extends Fragment {

    private OnListFragmentInteractionListener mListener;
    private static final String TAG = SearchResultsFragment.class.getSimpleName();
    SharedPreferences session;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);
        Log.d(TAG, "onCreate() " + view.getClass().getName());
        session = getActivity().getSharedPreferences(MainActivity.APP_SESSION, Context.MODE_PRIVATE);
        // Set the adapter
        if (view instanceof ListViewCompat || view instanceof ListView) {
            Context context = view.getContext();
            ListView listView = (ListView) view;
            ArrayList<Fact> results = new ArrayList<>();
            results.add(new Fact("No search results...", -1));
            SearchResultsAdapter adapter = new SearchResultsAdapter(context, results);
            listView.setAdapter(adapter);
        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(Object item);
    }

    public class SearchResultsAdapter extends ArrayAdapter<Fact> {

            public SearchResultsAdapter(Context context, ArrayList<Fact> results) {
                super(context, 0, results);
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // Get the data item for this position
                Fact result = getItem(position);
                // Check if an existing view is being reused, otherwise inflate the view
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.search_result, parent, false);
                }
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
                        ActionBar actionBar = ((MainActivity) SearchResultsFragment.this.getActivity()).getSupportActionBar();
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
