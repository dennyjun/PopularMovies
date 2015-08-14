package com.example.android.popularmovies.asynctasks;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.AsyncTask;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.adapters.recyclerview.MoviePosterAdapter;
import com.example.android.popularmovies.data.Movie;
import com.example.android.popularmovies.providers.MovieContentProvider;

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
        final Cursor cursor = context.getContentResolver().query(
                MovieContentProvider.CONTENT_URI,
                null,
                null,
                null,
                context.getString(R.string.moviedb_title_param) + " ASC");

        final List<Movie> movies = new LinkedList<>();
        while(cursor.moveToNext()) {
            final ContentValues contentValues = new ContentValues();
            DatabaseUtils.cursorRowToContentValues(cursor, contentValues);
            final Movie movie = new Movie(context, contentValues);
            movies.add(movie);
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
}
