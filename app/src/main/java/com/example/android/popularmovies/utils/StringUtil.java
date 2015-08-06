package com.example.android.popularmovies.utils;

import android.util.Log;

import java.io.UnsupportedEncodingException;

/**
 * Created by Denny on 8/2/2015.
 */
public class StringUtil {
    private static final String LOG_TAG = StringUtil.class.getSimpleName();

    public static String toUtf8(final String str) {
        try {
            return new String(str.getBytes("ISO-8859-1"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(LOG_TAG, "Unable to convert string to UTF-8.", e);
            return str;
        }
    }
}
