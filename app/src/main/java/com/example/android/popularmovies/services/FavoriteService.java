package com.example.android.popularmovies.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.example.android.popularmovies.receivers.FavoriteReceiver;

/**
 * Created by Denny on 7/28/2015.
 */
public class FavoriteService extends IntentService {
    private static final String LOG_TAG = FavoriteService.class.getSimpleName();
    public static final String INTENT_CMD_PARAM = "favCmd";

    public static final String CMD_ADD_FAV = "addFav";
    public static final String CMD_REM_FAV = "remFav";
    public static final String CMD_ADD_TRAILERS = "addTrailers";

    public FavoriteService() {
        super(GetMovieService.class.getSimpleName());
    }
    public FavoriteService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final Intent broadcastIntent = new Intent(FavoriteReceiver.class.getCanonicalName());
        final String cmd = intent.getStringExtra(INTENT_CMD_PARAM);
        broadcastIntent.putExtra(INTENT_CMD_PARAM, intent.getStringExtra(INTENT_CMD_PARAM));
        switch (cmd) {
            case CMD_ADD_FAV:
            case CMD_REM_FAV:
                broadcastIntent.putExtra(Intent.EXTRA_STREAM,
                        intent.getParcelableExtra(Intent.EXTRA_STREAM));
                break;
            case CMD_ADD_TRAILERS:
                broadcastIntent.putExtra(Intent.EXTRA_STREAM,
                        intent.getParcelableArrayExtra(Intent.EXTRA_STREAM));
                break;
            default: Log.e(LOG_TAG, "Invalid command.");
        }

        sendBroadcast(broadcastIntent);
    }
}

