package com.example.android.popularmovies.asynctasks;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popularmovies.data.MovieTrailer;
import com.example.android.popularmovies.R;
import com.example.android.popularmovies.utils.AppUtil;
import com.example.android.popularmovies.utils.MovieDbUtil;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by Denny on 8/1/2015.
 */
public class GetMovieTrailersTask extends GetMovieDataTask<MovieTrailer> {
    private static final String LOG_TAG = GetMovieTrailersTask.class.getSimpleName();

    private final LinearLayout movieTrailerLinearLayout;

    public GetMovieTrailersTask(final Context context,
                                final LinearLayout movieTrailerLinearLayout) {
        super(context);
        this.movieTrailerLinearLayout = movieTrailerLinearLayout;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        updateProgressBar(View.VISIBLE);
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

        int trailerNum = 1;
        for(final MovieTrailer movieTrailer : movieTrailers) {
            if(!isVideoATrailer(movieTrailer)) {
                continue;
            }
            final Button viewTrailerButton =
                    createViewTrailerButton(trailerNum++, movieTrailer.getUrl());
            movieTrailerLinearLayout.addView(viewTrailerButton);
        }
        updateProgressBar(View.GONE);
        if(!AppUtil.isConnectedToInternet(context)) {
            showNoInternetMsg();
        } else if(noTrailersFound()) {
            showNoTrailersTextView();
        }
    }

    private boolean noTrailersFound() {
        return movieTrailerLinearLayout.getChildCount() == 1;
    }

    private void showNoTrailersTextView() {
        final TextView noTrailersTv = (TextView) movieTrailerLinearLayout
                .findViewById(R.id.movie_trailer_na_textview);
        noTrailersTv.setVisibility(View.VISIBLE);
    }

    private void updateProgressBar(final int visibility) {
        final View parent = (View)movieTrailerLinearLayout.getParent();
        final View progressBar =
                parent.findViewById(R.id.loading_trailer_buttons_spinner);
        progressBar.setVisibility(visibility);
    }

    private Button createViewTrailerButton(final int trailerNum, final String url) {
        final Button b = (Button) LayoutInflater.from(context)
                .inflate(R.layout.movie_trailer_item, movieTrailerLinearLayout, false);
        b.setText(context.getString(R.string.moviedb_trailer_button_label_prefix)
                + trailerNum);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse(url)));
            }
        });
        return b;
    }

    private boolean isVideoATrailer(final MovieTrailer movieTrailer) {
        return movieTrailer.getType().equals(
                context.getString(R.string.moviedb_trailer_type_trailer));
    }

    private void showNoInternetMsg() {
        Toast.makeText(
                context,
                "Failed to load trailers! Please check your internet connection.",
                Toast.LENGTH_LONG).show();
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
