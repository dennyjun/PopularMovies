package com.example.android.popularmovies.database;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.database.DbUpdate;
import com.example.android.popularmovies.providers.MovieContentProvider;
import com.example.android.popularmovies.utils.AppUtil;
import com.example.android.popularmovies.utils.MovieDbUtil;

/**
 * Created by Denny on 8/20/2015.
 */
public class FavoriteMovieUpdate extends DbUpdate {
    @Override
    public String buildDownloadUrl(Context context, ContentValues contentValues) {
        final String id = contentValues.getAsString(context.getString(R.string.moviedb_id_param));
        final Uri.Builder baseUrl = MovieDbUtil.getMovieBaseUri(context);
        baseUrl.appendPath(id);
        baseUrl.appendQueryParameter(context.getString(R.string.moviedb_api_key_param),
                AppUtil.getMetaDataString(context, R.string.moviedb_api_key_meta_data));
        return baseUrl.build().toString();
    }

    @Override
    public Uri getUri(Context context) {
        return MovieContentProvider.FAVORITES_CONTENT_URI;
    }

    @Override
    public String getWhereStatement(Context context) {
        return context.getString(R.string.moviedb_id_param) + "=?";
    }

    @Override
    public String[] getSelectionArgs(final Context context, ContentValues contentValues) {
        final String movieId = context.getString(R.string.moviedb_id_param);
        return new String[]{contentValues.getAsString(movieId)};
    }
}
