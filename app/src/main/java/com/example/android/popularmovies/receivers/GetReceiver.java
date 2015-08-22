package com.example.android.popularmovies.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Denny on 8/19/2015.
 */
public abstract class GetReceiver extends BroadcastReceiver {
    public static final String MOVIE_DOWNLOAD_INTENT = "MOVIE_DOWNLOAD";
    public static final String TRAILER_DOWNLOAD_INTENT = "TRAILER_DOWNLOAD";
    public static final String REVIEW_DOWNLOAD_INTENT = "REVIEW_DOWNLOAD";

    @Override
    public void onReceive(Context context, Intent intent) {
        onDownloadComplete(context, intent);
    }

    public abstract void onDownloadComplete(Context context, Intent intent);
}
