package com.example.android.popularmovies.receivers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;

/**
 * Created by Denny on 8/20/2015.
 */
public class ManagedReceiver {
    private final BroadcastReceiver broadcastReceiver;
    private final IntentFilter intentFilter;

    public ManagedReceiver(final BroadcastReceiver broadcastReceiver,
                           final IntentFilter intentFilter) {
        this.broadcastReceiver = broadcastReceiver;
        this.intentFilter = intentFilter;
    }

    public void register(final Activity activity) {
        activity.registerReceiver(broadcastReceiver, intentFilter);
    }

    public void unregister(final Activity activity) {
        activity.unregisterReceiver(broadcastReceiver);
    }
}
