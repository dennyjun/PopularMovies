package com.example.android.popularmovies.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.adapters.recyclerview.MoviePosterAdapter;
import com.example.android.popularmovies.data.Movie;
import com.example.android.popularmovies.utils.AppUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Denny on 7/28/2015.
 */
public class GetMovieReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = GetMovieReceiver.class.getSimpleName();

    private final MoviePosterAdapter moviePosterAdapter;

    public GetMovieReceiver(final MoviePosterAdapter moviePosterAdapter) {
        this.moviePosterAdapter = moviePosterAdapter;
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        moviePosterAdapter.hideProgressBar();

        try {
            if (!AppUtil.isConnectedToInternet(context)) {
                showNoInternetMsg(context);
                return;
            }
            final String movieJsonStr = intent.getStringExtra(Intent.EXTRA_TEXT);
            final JSONObject obj = new JSONObject(movieJsonStr);
            final JSONArray results = obj.getJSONArray(
                    context.getString(R.string.moviedb_data_results_json_array_param));
            for(int i = 0; i < results.length(); ++i) {
                moviePosterAdapter.addItem(new Movie(context, results.getJSONObject(i)));
            }
            moviePosterAdapter.incrementPage();
            final int totalPages = obj.getInt(context.getString(R.string.moviedb_data_total_pages_param));
            moviePosterAdapter.setNoMoreData(moviePosterAdapter.getPage() > totalPages);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Unable to extract movie data from JSON string.", e);
        } finally {
            moviePosterAdapter.setLoading(false);
        }
    }

    private void showNoInternetMsg(Context context) {
        Toast.makeText(
                context,
                "Failed to load movies! Please check your internet connection.",
                Toast.LENGTH_LONG).show();
    }
}
