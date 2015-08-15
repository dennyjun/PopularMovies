package com.example.android.popularmovies.asynctasks;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.adapters.recyclerview.MoviePosterAdapter;
import com.example.android.popularmovies.data.Movie;
import com.example.android.popularmovies.providers.MovieContentProvider;
import com.example.android.popularmovies.utils.AppUtil;
import com.example.android.popularmovies.utils.MovieDbUtil;
import com.example.android.popularmovies.utils.WebUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Denny on 8/13/2015.
 */
public class GetFavoriteMoviesTask extends AsyncTask<String, Void, List<Movie>> {
    private static final String LOG_TAG = GetFavoriteMoviesTask.class.getSimpleName();
    private final MoviePosterAdapter moviePosterAdapter;
    private final Context context;

    public GetFavoriteMoviesTask(Context context,
                                 MoviePosterAdapter moviePosterAdapter) {
        this.context = context;
        this.moviePosterAdapter = moviePosterAdapter;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        moviePosterAdapter.showProgressBar();
    }

    @Override
    protected List<Movie> doInBackground(String... params) {
        final List<Movie> movies = new LinkedList<>();

        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    MovieContentProvider.FAVORITES_CONTENT_URI,
                    null,
                    null,
                    null,
                    context.getString(R.string.moviedb_title_param) + " ASC");

            while (cursor.moveToNext()) {
                final ContentValues contentValues = new ContentValues();
                DatabaseUtils.cursorRowToContentValues(cursor, contentValues);
                if (AppUtil.isConnectedToInternet(context)) {                                       // Update favorite movie data in the database whenever there's internet
                    final String movieId =
                            contentValues.getAsString(context.getString(R.string.moviedb_id_param));
                    movies.add(update(movieId));
                } else {
                    movies.add(new Movie(context, contentValues));
                }
            }
        } finally {
            if(cursor != null) cursor.close();
        }
        return movies;
    }

    @Override
    protected void onPostExecute(final List<Movie> movies) {
        super.onPostExecute(movies);
        moviePosterAdapter.hideProgressBar();
        moviePosterAdapter.addItems(movies);
        moviePosterAdapter.setNoMoreData(true);
        moviePosterAdapter.setLoading(false);
    }

    private Movie update(final String movieId) {
        final String request = buildUrl(context, movieId);
        final String result = WebUtil.get(request);
        final Movie movie = createFromJson(result);

        context.getContentResolver().update(
                MovieContentProvider.FAVORITES_CONTENT_URI,
                movie.createContentValues(context),
                context.getString(R.string.moviedb_id_param) + "=?",
                new String[]{movie.getId()});
        return movie;
    }

    private String buildUrl(final Context c, final String id) {
        final Uri.Builder baseUrl = MovieDbUtil.getMovieBaseUri(c);
        baseUrl.appendPath(id);
        baseUrl.appendQueryParameter(c.getString(R.string.moviedb_api_key_param),
                AppUtil.getMetaDataString(c, R.string.moviedb_api_key_meta_data));
        return baseUrl.build().toString();
    }

    private Movie createFromJson(final String jsonStr) {
        try {
            final JSONObject obj = new JSONObject(jsonStr);
            return new Movie(context, obj);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Failed to get movie data using JSON.", e);
        }
        return null;
    }
}
