package com.example.android.popularmovies.asynctasks;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.widget.Toast;

import com.dd.CircularProgressButton;
import com.example.android.popularmovies.R;
import com.example.android.popularmovies.data.MovieReview;
import com.example.android.popularmovies.utils.AppUtil;
import com.example.android.popularmovies.utils.MovieDbUtil;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by Denny on 8/4/2015.
 */
public class ToggleReadReviewsButtonTask extends GetMovieDataTask<MovieReview> {
    private final CircularProgressButton circularProgressButton;

    public ToggleReadReviewsButtonTask(final Context context, final CircularProgressButton circularProgressButton) {
        super(context);
        this.circularProgressButton = circularProgressButton;
    }

    @Override
    protected String buildRequestUrl(String... params) {
        final String id = params[0];
        return buildReviewsUrl(context, id);
    }

    @Override
    protected MovieReview createDataObject(JSONObject jsonObject) {
        return new MovieReview(context, jsonObject);
    }

    @Override
    protected void onPostExecute(List<MovieReview> movieReviews) {
        super.onPostExecute(movieReviews);

        if(!AppUtil.isConnectedToInternet(context)) {
            circularProgressButton.setVisibility(View.INVISIBLE);
            showNoInternetMsg();
            return;
        }
        if(getTotalResults() == 0) {
            circularProgressButton.setOnClickListener(null);
            circularProgressButton.setClickable(false);
            circularProgressButton.setProgress(-1); // displays error text
        } else {
            circularProgressButton.setProgress(100); // displays success text
        }
    }

    private void showNoInternetMsg() {
        Toast.makeText(
                context,
                "Failed to check for reviews! Please check your internet connection.",
                Toast.LENGTH_SHORT).show();
    }

    private String buildReviewsUrl(final Context c, final String id) {
        final Uri.Builder baseUrl = MovieDbUtil.getMovieBaseUri(c);
        baseUrl.appendPath(id);
        baseUrl.appendPath(c.getString(R.string.moviedb_review_path));
        baseUrl.appendQueryParameter(c.getString(R.string.moviedb_api_key_param),
                AppUtil.getMetaData(c, c.getString(R.string.moviedb_api_key_meta_data)));
        return baseUrl.build().toString();
    }
}
