package com.example.android.popularmovies.asynctasks;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.AsyncTask;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.adapters.recyclerview.MovieTrailerAdapter;
import com.example.android.popularmovies.data.MovieTrailer;
import com.example.android.popularmovies.databases.MovieDbHelper;
import com.example.android.popularmovies.providers.MovieContentProvider;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Denny on 8/14/2015.
 */
public class GetFavoriteTrailersTask extends AsyncTask<String, Void, List<MovieTrailer>> {
    private static final String LOG_TAG = GetFavoriteTrailersTask.class.getSimpleName();
    private final MovieTrailerAdapter movieTrailerAdapter;
    private final Context context;

    public GetFavoriteTrailersTask(Context context,
                                   MovieTrailerAdapter movieTrailerAdapter) {
        this.context = context;
        this.movieTrailerAdapter = movieTrailerAdapter;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        movieTrailerAdapter.showProgressBar();
    }

    @Override
    protected List<MovieTrailer> doInBackground(String... params) {
        final String movieId = params[0];
        final Cursor cursor = context.getContentResolver().query(
                MovieContentProvider.TRAILERS_CONTENT_URI,
                null,
                MovieDbHelper.FAVORITES_TABLE_NAME +
                        context.getString(R.string.moviedb_id_param) + "=?",
                new String[]{movieId},
                context.getString(R.string.moviedb_trailer_name_param) + " ASC");
        final List<MovieTrailer> trailers = new LinkedList<>();
        try {
            while (cursor.moveToNext()) {
                final ContentValues contentValues = new ContentValues();
                DatabaseUtils.cursorRowToContentValues(cursor, contentValues);
                final MovieTrailer trailer = new MovieTrailer(context, contentValues);
                trailers.add(trailer);
            }
        } finally {
            cursor.close();
        }
        return trailers;
    }

    @Override
    protected void onPostExecute(final List<MovieTrailer> trailers) {
        super.onPostExecute(trailers);
        movieTrailerAdapter.hideProgressBar();
        movieTrailerAdapter.addItems(trailers);
        if(movieTrailerAdapter.getItemCount() == 0) {
            movieTrailerAdapter.addItem(null);
        }
        movieTrailerAdapter.setNoMoreData(true);
        movieTrailerAdapter.setLoading(false);
    }
}
