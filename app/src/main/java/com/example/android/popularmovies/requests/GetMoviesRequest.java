package com.example.android.popularmovies.requests;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.data.Movie;
import com.example.android.popularmovies.receivers.GetReceiver;
import com.example.android.popularmovies.utils.AppUtil;

import org.json.JSONObject;

/**
 * Created by Denny on 8/19/2015.
 */
public class GetMoviesRequest extends GetRequest<Movie> {

    @Override
    public String buildDownloadUrl(Context context, Intent intent) {
        final String sortBy =
                intent.getStringExtra(context.getString(R.string.moviedb_sort_by_param));
        final String page = String.valueOf(
                intent.getIntExtra(context.getString(R.string.moviedb_page_param), 1));
        return getMoviesQuery(context, sortBy, page);
    }

    @Override
    protected Movie createDataObject(Context context, JSONObject jsonObject) {
        return new Movie(context, jsonObject);
    }

    @Override
    protected Movie createDataObject(Context context, ContentValues contentValues) {
        return new Movie(context, contentValues);
    }

    @Override
    public Intent getBroadcastReceiverIntent() {
        return new Intent(GetReceiver.MOVIE_DOWNLOAD_INTENT);
    }

    private String getMoviesQuery(Context context, final String sortBy, final String page) {
        final Uri.Builder builder = attachBaseFilters(context, getDiscoverMovieBaseUri(context));
        builder.appendQueryParameter(context.getString(R.string.moviedb_sort_by_param),
                sortBy + context.getString(R.string.moviedb_desc_affix));
        builder.appendQueryParameter(context.getString(R.string.moviedb_page_param), page);
        builder.appendQueryParameter(context.getString(R.string.moviedb_api_key_param),
                AppUtil.getMetaDataString(context, R.string.moviedb_api_key_meta_data));
        return builder.build().toString();
    }

    /**
     * 1. English movies only
     * 2. Vote count is greater than 100
     * @param context
     * @param builder
     * @return
     */
    private Uri.Builder attachBaseFilters(final Context context, final Uri.Builder builder) {
        builder.appendQueryParameter(context.getString(R.string.moviedb_language_param),
                context.getString(R.string.moviedb_language_english));
        builder.appendQueryParameter(context.getString(R.string.moviedb_vote_count_param) +
                        context.getString(R.string.moviedb_gte_affix),
                context.getString(R.string.moviedb_vote_count_min));
        return builder;
    }

    private Uri.Builder getDiscoverMovieBaseUri(final Context context) {
        final Uri.Builder builder = new Uri.Builder();
        builder.scheme(context.getString(R.string.moviedb_scheme_secure))
                .authority(context.getString(R.string.moviedb_authority))
                .appendPath(context.getString(R.string.moviedb_three_path))
                .appendPath(context.getString(R.string.moviedb_discover_path))
                .appendPath(context.getString(R.string.moviedb_movie_path));
        return builder;
    }
}
