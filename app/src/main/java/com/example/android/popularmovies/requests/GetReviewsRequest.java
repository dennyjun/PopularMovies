package com.example.android.popularmovies.requests;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.data.MovieReview;
import com.example.android.popularmovies.receivers.GetReceiver;
import com.example.android.popularmovies.utils.AppUtil;
import com.example.android.popularmovies.utils.MovieDbUtil;

import org.json.JSONObject;

/**
 * Created by Denny on 8/22/2015.
 */
public class GetReviewsRequest extends GetRequest<MovieReview> {
    @Override
    public String buildDownloadUrl(Context context, Intent intent) {
        final String movieId =
                intent.getStringExtra(context.getString(R.string.moviedb_id_param));
        final String page = intent.getStringExtra(context.getString(R.string.moviedb_page_param));

        final Uri.Builder baseUrl = MovieDbUtil.getMovieBaseUri(context);
        baseUrl.appendPath(movieId);
        baseUrl.appendPath(context.getString(R.string.moviedb_review_path));
        baseUrl.appendQueryParameter(context.getString(R.string.moviedb_api_key_param),
                AppUtil.getMetaDataString(context, R.string.moviedb_api_key_meta_data));
        baseUrl.appendQueryParameter(context.getString(R.string.moviedb_page_param), page);
        return baseUrl.build().toString();
    }

    @Override
    protected MovieReview createDataObject(Context context, JSONObject jsonObject) {
        return new MovieReview(context, jsonObject);
    }

    @Override
    protected MovieReview createDataObject(Context context, ContentValues contentValues) {
        return new MovieReview(context, contentValues);
    }

    @Override
    public Intent getBroadcastReceiverIntent() {
        return new Intent(GetReceiver.REVIEW_DOWNLOAD_INTENT);
    }
}
