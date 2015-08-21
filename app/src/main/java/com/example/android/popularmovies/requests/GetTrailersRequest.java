package com.example.android.popularmovies.requests;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.data.MovieTrailer;
import com.example.android.popularmovies.receivers.GetReceiver;
import com.example.android.popularmovies.utils.AppUtil;
import com.example.android.popularmovies.utils.MovieDbUtil;

import org.json.JSONObject;

/**
 * Created by Denny on 8/19/2015.
 */
public class GetTrailersRequest extends GetRequest<MovieTrailer> {
    @Override
    public String buildDownloadUrl(Context context, Intent intent) {
        final String movieId = intent.getStringExtra(context.getString(R.string.moviedb_id_param));
        return buildTrailersUrl(context, movieId);
    }

    @Override
    protected MovieTrailer createDataObject(Context context, JSONObject jsonObject) {
        return new MovieTrailer(context, jsonObject);
    }

    @Override
    protected MovieTrailer createDataObject(Context context, ContentValues contentValues) {
        return new MovieTrailer(context, contentValues);
    }

    @Override
    public Intent getBroadcastReceiverIntent() {
        return new Intent(GetReceiver.TRAILER_DOWNLOAD_INTENT);
    }

    private String buildTrailersUrl(final Context c, final String movieId) {
        final Uri.Builder baseUrl = MovieDbUtil.getMovieBaseUri(c);
        baseUrl.appendPath(movieId);
        baseUrl.appendPath(c.getString(R.string.moviedb_video_path));
        baseUrl.appendQueryParameter(c.getString(R.string.moviedb_api_key_param),
                AppUtil.getMetaDataString(c, R.string.moviedb_api_key_meta_data));
        return baseUrl.build().toString();
    }
}
