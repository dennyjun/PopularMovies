package com.example.android.popularmovies.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.example.android.popularmovies.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Denny on 7/28/2015.
 * Misc. application utilities
 */
public class AppUtil {
    private static final String LOG_TAG = AppUtil.class.getSimpleName();

    public static Bundle getMetaData(final Context c) {
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

    public static String getMetaDataString(final Context c, final int metaDataNameId) {
        final Bundle metaData = getMetaData(c);
        if(metaData == null) {
            return null;
        }
        return metaData.getString(c.getString(metaDataNameId));
    }

    public static int getMetaDataInt(final Context c, final int metaDataNameId) {
        final Bundle metaData = getMetaData(c);
        if(metaData == null) {
            return -1;
        }
        return metaData.getInt(c.getString(metaDataNameId));
    }

    public static boolean isConnectedToInternet(final Context context) {
        final ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    public static boolean isTabletLayout(final Context context) {
        return context.getResources().getBoolean(R.bool.has_two_panes);
    }

    public static String getSortMethodFromPref(final Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(
                context.getString(R.string.pref_sort_by_key),
                context.getString(R.string.moviedb_popularity_param));
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

    public static void removeFragment(final FragmentManager fm, final String fragmentTag) {
        final Fragment target = fm.findFragmentByTag(fragmentTag);
        if(target != null) {
            fm.beginTransaction().remove(target).commit();
        }
    }
}
