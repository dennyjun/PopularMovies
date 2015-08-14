package com.example.android.popularmovies.services;

import android.app.IntentService;
import android.content.Intent;

import com.example.android.popularmovies.receivers.FavoriteReceiver;

/**
 * Created by Denny on 7/28/2015.
 */
public class FavoriteService extends IntentService {

    public FavoriteService() {
        super(GetMovieService.class.getSimpleName());
    }
    public FavoriteService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final Intent broadcastIntent = new Intent(FavoriteReceiver.class.getCanonicalName());
        broadcastIntent.putExtra("command", intent.getStringExtra("command"));
        broadcastIntent.putExtra(Intent.EXTRA_STREAM,
                intent.getParcelableExtra(Intent.EXTRA_STREAM));
        sendBroadcast(broadcastIntent);
    }
}

