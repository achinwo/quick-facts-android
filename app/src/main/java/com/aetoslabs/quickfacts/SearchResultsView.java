package com.aetoslabs.quickfacts;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListAdapter;

public class SearchResultsView extends LinearLayout {

    private ListAdapter adapter;
    private DataSetObserver dataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            reloadChildViews();
        }
    };

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public SearchResultsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setOrientation(LinearLayout.VERTICAL);
    }

    public SearchResultsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(LinearLayout.VERTICAL);
    }

    public SearchResultsView(Context context) {
        super(context);
        setOrientation(LinearLayout.VERTICAL);
    }


    public void setAdapter(ListAdapter adapter) {
        if (this.adapter == adapter) return;
        this.adapter = adapter;
        if (adapter != null) adapter.registerDataSetObserver(dataSetObserver);
        reloadChildViews();
    }

    public ListAdapter getAdapter() {
        return adapter;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (adapter != null) adapter.unregisterDataSetObserver(dataSetObserver);
    }

    private void reloadChildViews() {
        removeAllViews();

        if (adapter == null) return;

        int count = adapter.getCount();
        for (int position = 0; position < count; ++position) {
            View v = adapter.getView(position, null, this);
            if (v != null) addView(v);
        }

        requestLayout();
    }

}
