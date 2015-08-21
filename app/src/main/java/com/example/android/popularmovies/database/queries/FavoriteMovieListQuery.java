package com.example.android.popularmovies.database.queries;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.database.queries.DbQuery;
import com.example.android.popularmovies.providers.MovieContentProvider;

/**
 * Created by Denny on 8/20/2015.
 */
public class FavoriteMovieListQuery extends DbQuery {
    @Override
    public Uri getUri(final Context context) {
        return MovieContentProvider.FAVORITES_CONTENT_URI;
    }

    @Override
    public String[] getProjection(final Context context) {
        return null;
    }

    @Override
    public String getSelection(final Context context) {
        return null;
    }

    @Override
    public String[] getSelectionArgs(final Context context, final Intent intent,
                                     final ContentValues contentValues) {
        return null;
    }

    @Override
    public String getSortOrder(final Context context) {
        return context.getString(R.string.moviedb_title_param) + " ASC";
    }
}
