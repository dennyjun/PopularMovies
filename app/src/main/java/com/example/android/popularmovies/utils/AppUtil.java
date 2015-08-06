package com.example.android.popularmovies.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Denny on 7/28/2015.
 */
public class AppUtil {
    private static final String LOG_TAG = AppUtil.class.getSimpleName();

    public static final String getMetaData(final Context c, final String metaDataName) {
        String metaDataValue = null;
        try {
            final ApplicationInfo appInfo =
                    c.getPackageManager()
                            .getApplicationInfo(c.getPackageName(), PackageManager.GET_META_DATA);
            final Bundle bundle = appInfo.metaData;
            metaDataValue = bundle.getString(metaDataName);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(LOG_TAG, "Failed to get metadata.", e);
        }
        return metaDataValue;
    }

    public static void setTextFromIntentString(final View rootView, final Intent intent,
                                               final int textViewId, final int intentNameId) {
        final TextView textView = (TextView) rootView.findViewById(textViewId);
        final String data = intent.getStringExtra(rootView.getContext().getString(intentNameId));
        textView.setText(data);
    }

    public static void setTextFromIntentInt(final View rootView, final Intent intent,
                                               final int textViewId, final int intentNameId) {
        final TextView textView = (TextView) rootView.findViewById(textViewId);
        final int data = intent.getIntExtra(rootView.getContext().getString(intentNameId), 0);
        textView.setText(String.valueOf(data));
    }

    public static void setTextFromIntentDouble(final View rootView, final Intent intent,
                                            final int textViewId, final int intentNameId) {
        setTextFromIntentDouble(rootView, intent, null, textViewId, intentNameId);
    }

    public static void setTextFromIntentDouble(final View rootView, final Intent intent,
                                               final DecimalFormat decimalFormat,
                                               final int textViewId, final int intentNameId) {
        final TextView textView = (TextView) rootView.findViewById(textViewId);
        final double data =
                intent.getDoubleExtra(rootView.getContext().getString(intentNameId), 0d);
        final String text = (decimalFormat == null)
                ? String.valueOf(data)
                : decimalFormat.format(data);
        textView.setText(text);
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
