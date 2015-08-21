package com.example.android.popularmovies.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.data.Movie;
import com.example.android.popularmovies.providers.MovieContentProvider;

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
        super(FavoriteService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final String cmd = intent.getStringExtra(INTENT_CMD_PARAM);
        switch (cmd) {
            case CMD_ADD_FAV:
                addFavorite(getBaseContext(), intent);
                break;
            case CMD_REM_FAV:
                removeFavorite(getBaseContext(), intent);
                break;
            case CMD_ADD_TRAILERS:
                addTrailers(getBaseContext(), intent);
                break;
            default: Log.e(LOG_TAG, "Invalid command.");
        }
    }

    private void addFavorite(final Context context, final Intent intent) {
        final Parcelable movieParcelable = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        final Movie movie = new Movie(context, (ContentValues) movieParcelable);
        context.getContentResolver().insert(MovieContentProvider.FAVORITES_CONTENT_URI,
                movie.createContentValues(context));
    }

    private void removeFavorite(final Context context, final Intent intent) {
        final Parcelable movieParcelable = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        final Movie movie = new Movie(context, (ContentValues) movieParcelable);
        context.getContentResolver().delete(MovieContentProvider.FAVORITES_CONTENT_URI,
                context.getString(R.string.moviedb_id_param) + "=?",
                new String[]{movie.getId()}
        );
    }

    private void addTrailers(final Context context, final Intent intent) {
        final Parcelable[] values = intent.getParcelableArrayExtra(Intent.EXTRA_STREAM);
        for(final Parcelable p : values) {
            if(p == null) {
                continue;
            }
            context.getContentResolver().insert(MovieContentProvider.TRAILERS_CONTENT_URI,
                    (ContentValues) p);
        }
    }
}

