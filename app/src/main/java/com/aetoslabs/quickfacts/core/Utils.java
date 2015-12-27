package com.aetoslabs.quickfacts.core;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by anthony on 20/12/15.
 */
public class Utils {
    private static final String TAG = Utils.class.getSimpleName();
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public static Date parseDate(String dateString) {
        SimpleDateFormat format = new SimpleDateFormat(DATE_TIME_FORMAT, Locale.UK);
        Date date = null;
        try {
            date = format.parse(dateString);

        } catch (Exception e) {
            Log.e(TAG, "Error parsing date '" + dateString + "': " + e.toString());
        }
        return date;
    }
}
