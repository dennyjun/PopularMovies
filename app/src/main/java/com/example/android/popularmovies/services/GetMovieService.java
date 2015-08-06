package com.example.android.popularmovies.services;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;

import com.example.android.popularmovies.utils.AppUtil;
import com.example.android.popularmovies.utils.WebUtil;
import com.example.android.popularmovies.R;

/**
 * Created by Denny on 7/28/2015.
 */
public class GetMovieService extends IntentService {

    public GetMovieService() {
        super(GetMovieService.class.getSimpleName());
    }
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public GetMovieService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final String sortBy = intent.getStringExtra(getString(R.string.moviedb_sort_by_param));
        final String page = String.valueOf(
                intent.getIntExtra(getString(R.string.moviedb_page_param), 1));
        final String movies = getMovies(sortBy, page);
        final Intent broadcastIntent = new Intent(getString(R.string.action_get_movie));
        broadcastIntent.putExtra(Intent.EXTRA_TEXT, movies);
        sendBroadcast(broadcastIntent);
    }

    private Uri.Builder getDiscoverMovieBaseUri() {
        final Uri.Builder builder = new Uri.Builder();
        builder.scheme(getString(R.string.moviedb_scheme_secure))
                .authority(getString(R.string.moviedb_authority))
                .appendPath(getString(R.string.moviedb_three_path))
                .appendPath(getString(R.string.moviedb_discover_path))
                .appendPath(getString(R.string.moviedb_movie_path));
        return builder;
    }

    private Uri.Builder attachBaseFilters(final Uri.Builder builder) {
        builder.appendQueryParameter(getString(R.string.moviedb_language_param),
                getString(R.string.moviedb_language_english));
        builder.appendQueryParameter(getString(R.string.moviedb_vote_count_param) +
                getString(R.string.moviedb_gte_affix), getString(R.string.moviedb_vote_count_min));
        return builder;
    }

    private String getMoviesQuery(final String sortBy, final String page) {
        final Uri.Builder builder = attachBaseFilters(getDiscoverMovieBaseUri());
        builder.appendQueryParameter(getString(R.string.moviedb_sort_by_param),
                sortBy + getString(R.string.moviedb_desc_affix));
        builder.appendQueryParameter(getString(R.string.moviedb_page_param), page);
        builder.appendQueryParameter(getString(R.string.moviedb_api_key_param),
                AppUtil.getMetaData(this, getString(R.string.moviedb_api_key_meta_data)));
        return builder.build().toString();
    }

    private String getMovies(final String sortBy, final String page) {
        return WebUtil.get(getMoviesQuery(sortBy, page));
    }
}
