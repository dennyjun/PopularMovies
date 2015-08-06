package com.example.android.popularmovies.utils;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Denny on 7/28/2015.
 */
public class WebUtil {
    private static final String LOG_TAG = WebUtil.class.getSimpleName();

    public static final String get(final String urlStr) {
        HttpURLConnection urlConnection = null;
        final StringBuilder sb = new StringBuilder();
        try {
            final URL url = new URL(urlStr);
            urlConnection = (HttpURLConnection)url.openConnection();
            final InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            int c;
            while((c = in.read()) != -1) {
                sb.append((char)c);
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Unable to connect: " + urlStr, e);
        } finally {
            if(urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return sb.toString();
    }
}
