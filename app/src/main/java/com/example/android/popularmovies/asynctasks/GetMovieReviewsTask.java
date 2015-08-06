package com.example.android.popularmovies.asynctasks;

import android.content.Context;
import android.net.Uri;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.android.popularmovies.utils.AppUtil;
import com.example.android.popularmovies.utils.MovieDbUtil;
import com.example.android.popularmovies.R;
import com.example.android.popularmovies.adapters.recyclerview.MovieReviewAdapter;
import com.example.android.popularmovies.data.MovieReview;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by Denny on 8/2/2015.
 */
public class GetMovieReviewsTask extends GetMovieDataTask<MovieReview> {
    private final MovieReviewAdapter movieReviewAdapter;
    private final ProgressBar progressBar;

    public GetMovieReviewsTask(Context context,
                               MovieReviewAdapter movieMovieReviewAdapter,
                               ProgressBar progressBar) {
        super(context);
        this.movieReviewAdapter = movieMovieReviewAdapter;
        this.progressBar = progressBar;
    }

    @Override
    protected String buildRequestUrl(String... params) {
        final String id = params[0];
        final String page = params[1];
        return buildReviewsUrl(context, id, page);
    }

    @Override
    protected MovieReview createDataObject(JSONObject jsonObject) {
        return new MovieReview(context, jsonObject);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPostExecute(List<MovieReview> movieReviews) {
        super.onPostExecute(movieReviews);
        movieReviewAdapter.removeLast();
        movieReviewAdapter.addItems(movieReviews);
        movieReviewAdapter.finalizeDataChange();
        movieReviewAdapter.setNoMoreData(movieReviewAdapter.getPage() > getTotalPages());
        if(movieReviewAdapter.isNoMoreData() && movieReviewAdapter.getItemCount() == 0) {
            movieReviewAdapter.addItem(null);
        }
    }

    private void showNoInternetMsg() {
        Toast.makeText(
                context,
                "Failed to load trailers! Please check your internet connection.",
                Toast.LENGTH_LONG).show();
    }

    private String buildReviewsUrl(final Context c, final String id, final String page) {
        final Uri.Builder baseUrl = MovieDbUtil.getMovieBaseUri(c);
        baseUrl.appendPath(id);
        baseUrl.appendPath(c.getString(R.string.moviedb_review_path));
        baseUrl.appendQueryParameter(c.getString(R.string.moviedb_api_key_param),
                AppUtil.getMetaData(c, c.getString(R.string.moviedb_api_key_meta_data)));
        baseUrl.appendQueryParameter(c.getString(R.string.moviedb_page_param), page);
        return baseUrl.build().toString();
    }
}
