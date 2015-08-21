package com.example.android.popularmovies.database.queries;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.database.helper.MovieDbHelper;
import com.example.android.popularmovies.database.queries.DbQuery;
import com.example.android.popularmovies.providers.MovieContentProvider;

/**
 * Created by Denny on 8/20/2015.
 */
public class FavoriteTrailersQuery extends DbQuery {
    @Override
    public Uri getUri(Context context) {
        return MovieContentProvider.TRAILERS_CONTENT_URI;
    }

    @Override
    public String[] getProjection(Context context) {
        return null;
    }

    @Override
    public String getSelection(Context context) {
        return MovieDbHelper.FAVORITES_TABLE_NAME +
                context.getString(R.string.moviedb_id_param) + "=?";
    }

    @Override
    public String[] getSelectionArgs(Context context, final Intent intent,
                                     final ContentValues contentValues) {
        final String movieId = intent.getStringExtra(context.getString(R.string.moviedb_id_param));
        return new String[]{movieId};
    }

    @Override
    public String getSortOrder(Context context) {
        return context.getString(R.string.moviedb_trailer_name_param) + " ASC";
    }
}
