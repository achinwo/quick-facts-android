package com.aetoslabs.quickfacts.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.ListViewCompat;
import android.util.Log;
import android.view.ActionMode;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
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

            // First create the GestureListener that will include all our callbacks.
            // Then create the GestureDetector, which takes that listener as an argument.
//            GestureDetector.SimpleOnGestureListener gestureListener = new GestureListener();
//            final GestureDetector gd = new GestureDetector(getActivity(), gestureListener);

                /* For the view where gestures will occur, create an onTouchListener that sends
                 * all motion events to the gesture detector.  When the gesture detector
                 * actually detects an event, it will use the callbacks you created in the
                 * SimpleOnGestureListener to alert your application.
                */

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
                View view = convertView;
                Context context = view.getContext();
                view.setClickable(true);
                view.setFocusable(true);

//                view.setOnTouchListener(new OnSwipeTouchListener(context) {
//                    @Override
//                    public void onSwipeLeft() {
//                        Log.d(TAG, "touched");
//                    }
//                });

                view.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        Log.d(TAG, "motion " + event.toString());
                        ViewGroup.LayoutParams p = v.getLayoutParams();
                        p.width = p.width - 1;
                        v.setLayoutParams(p);
                        return true;
                    }
                });

                view.setOnDragListener(new View.OnDragListener() {
                    @Override
                    public boolean onDrag(View v, DragEvent event) {
                        Log.d(TAG, "dragg " + event.toString());
                        return true;
                    }
                });

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
                        final ActionBar actionBar = ((MainActivity) SearchResultsFragment.this.getActivity()).getSupportActionBar();
                        if (actionBar != null) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    SearchResultsFragment.this.getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            actionBar.show();
                                        }
                                    });
                                }
                            }, 250);
                        }
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
