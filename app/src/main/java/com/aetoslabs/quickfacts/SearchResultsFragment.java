package com.aetoslabs.quickfacts;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.ListViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class SearchResultsFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    private OnListFragmentInteractionListener mListener;
    private static final String TAG = Fragment.class.getSimpleName();

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SearchResultsFragment() {
    }

    public static SearchResultsFragment newInstance(int columnCount) {
        SearchResultsFragment fragment = new SearchResultsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);
        Log.d(TAG, "onCreate() " + view.getClass().getName());
        // Set the adapter
        if (view instanceof ListViewCompat) {
            Context context = view.getContext();
            ListView listView = (ListView) view;
            ArrayList<Fact> results = new ArrayList<Fact>();
            results.add(new Fact("No search results...", "-1"));
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
                // Lookup view for data population
                TextView tvName = (TextView) convertView.findViewById(R.id.content);
                // Populate the data into the template view using the data object
                tvName.setText(result.content);
                // Return the completed view to render on screen
                return convertView;
            }
    }
}
