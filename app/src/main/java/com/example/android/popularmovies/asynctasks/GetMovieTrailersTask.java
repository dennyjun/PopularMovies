package com.example.android.popularmovies.asynctasks;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.adapters.recyclerview.MovieTrailerAdapter;
import com.example.android.popularmovies.data.MovieTrailer;
import com.example.android.popularmovies.utils.AppUtil;
import com.example.android.popularmovies.utils.MovieDbUtil;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by Denny on 8/1/2015.
 */
public class GetMovieTrailersTask extends GetMovieDataTask<MovieTrailer> {
    private static final String LOG_TAG = GetMovieTrailersTask.class.getSimpleName();

    private final MovieTrailerAdapter movieTrailerAdapter;

    public GetMovieTrailersTask(final Context context,
                                final MovieTrailerAdapter movieTrailerAdapter) {
        super(context);
        this.movieTrailerAdapter = movieTrailerAdapter;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String buildRequestUrl(String... params) {
        final String id = params[0];
        return buildTrailersUrl(context, id);
    }

    @Override
    protected MovieTrailer createDataObject(JSONObject jsonObject) {
        return new MovieTrailer(context, jsonObject);
    }

    @Override
    protected void onPostExecute(List<MovieTrailer> movieTrailers) {
        super.onPostExecute(movieTrailers);

        try {
            if (!AppUtil.isConnectedToInternet(context)) {
                showNoInternetMsg();
                return;
            }

            for (final MovieTrailer movieTrailer : movieTrailers) {
                if (!isVideoATrailer(movieTrailer)) {
                    continue;
                }
                movieTrailerAdapter.addItem(movieTrailer);
            }
        } finally {
            movieTrailerAdapter.setLoading(false);
        }
    }

    private boolean isVideoATrailer(final MovieTrailer movieTrailer) {
        return movieTrailer.getType().equals(
                context.getString(R.string.moviedb_trailer_type_trailer));
    }

    private void showNoInternetMsg() {
        Toast.makeText(
                context,
                "Failed to load trailers! Please check your internet connection.",
                Toast.LENGTH_SHORT).show();
    }

    private String buildTrailersUrl(final Context c, final String id) {
        final Uri.Builder baseUrl = MovieDbUtil.getMovieBaseUri(c);
        baseUrl.appendPath(id);
        baseUrl.appendPath(c.getString(R.string.moviedb_video_path));
        baseUrl.appendQueryParameter(c.getString(R.string.moviedb_api_key_param),
                AppUtil.getMetaData(c, c.getString(R.string.moviedb_api_key_meta_data)));
        return baseUrl.build().toString();
    }
}
