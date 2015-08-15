package com.example.android.popularmovies.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Denny on 7/28/2015.
 */
public class AppUtil {
    private static final String LOG_TAG = AppUtil.class.getSimpleName();

    public static final Bundle getMetaData(final Context c) {
        try {
            final ApplicationInfo appInfo =
                    c.getPackageManager()
                            .getApplicationInfo(c.getPackageName(), PackageManager.GET_META_DATA);
            return appInfo.metaData;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(LOG_TAG, "Failed to get metadata.", e);
        }
        return null;
    }

    public static final String getMetaDataString(final Context c, final int metaDataNameId) {
        final Bundle metaData = getMetaData(c);
        return metaData.getString(c.getString(metaDataNameId));
    }

    public static final int getMetaDataInt(final Context c, final int metaDataNameId) {
        final Bundle metaData = getMetaData(c);
        return metaData.getInt(c.getString(metaDataNameId));
    }

    public static final boolean isConnectedToInternet(final Context context) {
        final ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    public static String convertDateString(final String date,
                                           final String inFormat, final String outFormat) {
        try {
            final Date d = new SimpleDateFormat(inFormat).parse(date);
            return new SimpleDateFormat(outFormat).format(d);
        } catch (ParseException e) {
            Log.e(LOG_TAG, "Failed to convert date string.", e);
        }
        return date;
    }
}
