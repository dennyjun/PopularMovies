package com.example.android.popularmovies.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.android.popularmovies.adapters.listview.MovieListAdapter;
import com.example.android.popularmovies.data.Movie;
import com.example.android.popularmovies.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Denny on 7/28/2015.
 */
public class GetMovieReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = GetMovieReceiver.class.getSimpleName();

    private final MovieListAdapter movieListAdapter;

    public GetMovieReceiver(final MovieListAdapter movieListAdapter) {
        this.movieListAdapter = movieListAdapter;
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        final String movieJsonStr = intent.getStringExtra(Intent.EXTRA_TEXT);
        try {
            final JSONObject obj = new JSONObject(movieJsonStr);
            final JSONArray results = obj.getJSONArray(
                    context.getString(R.string.moviedb_data_results_json_array_param));
            for(int i = 0; i < results.length(); ++i) {
                movieListAdapter.addItem(new Movie(context, results.getJSONObject(i)));
            }
            movieListAdapter.setTotalPages(obj.getInt(context.getString(R.string.moviedb_data_total_pages_param)));
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Unable to extract movie data from JSON string.", e);
        }
        movieListAdapter.finalizeDataChange();
    }
}
