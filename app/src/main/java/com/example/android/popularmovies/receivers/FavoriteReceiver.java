package com.example.android.popularmovies.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.adapters.recyclerview.MoviePosterAdapter;
import com.example.android.popularmovies.data.Movie;
import com.example.android.popularmovies.providers.MovieContentProvider;

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
        final String action = intent.getStringExtra("command");
        final Movie movie = new Movie(context, intent.getParcelableExtra(Intent.EXTRA_STREAM));
        if(action.equals("add")) {
            addFavorite(context, movie);
        } else if(action.equals("remove")) {
            removeFavorite(context, movie);
        } else {
            Log.e(LOG_TAG, "Unable to process favorite movie:" + movie.getId() + ". Invalid action.");
        }
    }

    private void addFavorite(final Context context, final Movie movie) {
            context.getContentResolver().insert(MovieContentProvider.CONTENT_URI,
                    movie.createContentValues(context));
    }

    private void removeFavorite(final Context context, final Movie movie) {
        context.getContentResolver().delete(MovieContentProvider.CONTENT_URI,
                context.getString(R.string.moviedb_id_param) + "=?",
                new String[]{movie.getId()}
        );
        moviePosterAdapter.removeItem(movie);
    }
}
