package com.example.android.popularmovies.utils;

import android.content.Context;
import android.net.Uri;

import com.example.android.popularmovies.R;

/**
 * Created by Denny on 8/4/2015.
 */
public class MovieDbUtil {
    public static final Uri.Builder getMovieBaseUri(final Context c) {
        final Uri.Builder builder = new Uri.Builder();
        builder.scheme(c.getString(R.string.moviedb_scheme_secure))
                .authority(c.getString(R.string.moviedb_authority))
                .appendPath(c.getString(R.string.moviedb_three_path))
                .appendPath(c.getString(R.string.moviedb_movie_path));
        return builder;
    }
}
