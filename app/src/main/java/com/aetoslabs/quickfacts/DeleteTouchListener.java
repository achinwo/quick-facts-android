package com.aetoslabs.quickfacts;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;


/**
 * Created by anthony on 28/12/15.
 */
public class DeleteTouchListener implements View.OnTouchListener {

    public interface DeleteCallback {
        void onDelete(View view);
    }

    private final String TAG = DeleteTouchListener.class.getSimpleName();
    View mView;
    int mOrigMarginStart;
    boolean isDown = false;
    private float startY, startX;
    DeleteCallback mDeleteCallback;
    int THRESHOLD_DELETE = 360;

    public DeleteTouchListener(View view, DeleteCallback deleteHandler) {
        mView = view;
        mOrigMarginStart = getMarginStart();
        mDeleteCallback = deleteHandler;
    }

    public int getMarginStart() {
        int margin = 0;
        ViewGroup.LayoutParams params = mView.getLayoutParams();
        if (params instanceof LinearLayout.LayoutParams) {
            margin = ((LinearLayout.LayoutParams) params).getMarginStart();
        } else if (params instanceof RelativeLayout.LayoutParams) {
            margin = ((RelativeLayout.LayoutParams) params).getMarginStart();
        }
        return margin;
    }

    public void setMarginStart(int marginStart) {
        ViewGroup.LayoutParams params = mView.getLayoutParams();
        if (params instanceof LinearLayout.LayoutParams) {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) params;
            lp.setMarginStart(marginStart);
            mView.setLayoutParams(lp);
        } else if (params instanceof RelativeLayout.LayoutParams) {
            RelativeLayout.LayoutParams rp = (RelativeLayout.LayoutParams) params;
            rp.setMarginStart(marginStart);
            mView.setLayoutParams(rp);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int act = event.getAction();
        Log.d(TAG, event.toString());

        if (act == MotionEvent.ACTION_DOWN) {
            isDown = true;
            startX = event.getX();
            startY = event.getY();
        }

        if (isDown && act == MotionEvent.ACTION_MOVE) {
            double distY = Math.abs(event.getY() - startY);
            int distX = (int) (event.getX() - startX);

            double THRESHOLD_Y = 45.0;
            if (distY < THRESHOLD_Y) {
                if (event.getX() > startX) setMarginStart(mOrigMarginStart + distX);
                if (distX > THRESHOLD_DELETE && mDeleteCallback != null)
                    mDeleteCallback.onDelete(v);
            } else {
                isDown = false;
                setMarginStart(mOrigMarginStart);
            }
        }

        if (isDown && act == MotionEvent.ACTION_UP) {
            isDown = false;
            float endX = event.getX();
            setMarginStart(mOrigMarginStart);
        }
        return true;
    }
}
