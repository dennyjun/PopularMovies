package com.example.android.popularmovies.receivers;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.adapters.recyclerview.MoviePosterAdapter;
import com.example.android.popularmovies.data.Movie;
import com.example.android.popularmovies.providers.MovieContentProvider;
import com.example.android.popularmovies.services.FavoriteService;

/**
 * Created by Denny on 8/13/2015.
 */
public class FavoriteReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = FavoriteReceiver.class.getSimpleName();

    private final MoviePosterAdapter moviePosterAdapter;

    public FavoriteReceiver(final MoviePosterAdapter moviePosterAdapter) {
        this.moviePosterAdapter = moviePosterAdapter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String cmd = intent.getStringExtra(FavoriteService.INTENT_CMD_PARAM);
        switch (cmd) {
            case FavoriteService.CMD_ADD_FAV:
                addFavorite(context, intent);
                break;
            case FavoriteService.CMD_REM_FAV:
                removeFavorite(context, intent);
                break;
            case FavoriteService.CMD_ADD_TRAILERS:
                addTrailers(context, intent);
                break;
            default: Log.e(LOG_TAG, "Invalid cmd");
        }
    }

    private void addFavorite(final Context context, final Intent intent) {
        final Movie movie = new Movie(context, intent.getParcelableExtra(Intent.EXTRA_STREAM));
            context.getContentResolver().insert(MovieContentProvider.FAVORITES_CONTENT_URI,
                    movie.createContentValues(context));
    }

    private void removeFavorite(final Context context, final Intent intent) {
        final Movie movie = new Movie(context, intent.getParcelableExtra(Intent.EXTRA_STREAM));
        context.getContentResolver().delete(MovieContentProvider.FAVORITES_CONTENT_URI,
                context.getString(R.string.moviedb_id_param) + "=?",
                new String[]{movie.getId()}
        );
        if(moviePosterAdapter.getSortMethodFromPref().equals(
                context.getString(R.string.pref_sort_by_favorites))) {
            moviePosterAdapter.removeItem(movie);
        }
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
