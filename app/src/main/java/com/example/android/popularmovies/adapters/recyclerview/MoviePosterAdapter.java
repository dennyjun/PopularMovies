package com.example.android.popularmovies.adapters.recyclerview;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.android.popularmovies.R;
import com.example.android.popularmovies.callbacks.OpenMovieDetailsCallback;
import com.example.android.popularmovies.data.Movie;
import com.example.android.popularmovies.requests.FavoriteMovieListQuery;
import com.example.android.popularmovies.requests.FavoriteMovieQuery;
import com.example.android.popularmovies.requests.FavoriteMovieUpdate;
import com.example.android.popularmovies.requests.GetMovieListRequest;
import com.example.android.popularmovies.requests.GetRequest;
import com.example.android.popularmovies.services.GetService;
import com.example.android.popularmovies.utils.AppUtil;
import com.example.android.popularmovies.utils.SqlUtil;
import com.example.android.popularmovies.utils.WebUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by Denny on 8/6/2015.
 * Displays movie posters
 */
public class MoviePosterAdapter extends BaseRecyclerAdapter<Movie> {
    private static final String LOG_TAG = MoviePosterAdapter.class.getSimpleName();

    private String sortMethod = null;
    private transient OpenMovieDetailsCallback openMovieDetailsCallback;

    public MoviePosterAdapter(Context context) {
        super(context);
    }

    public class NormalViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView posterImageView;
        public NormalViewHolder(View listItem) {
            super(listItem);
            posterImageView = (ImageView) listItem;
            posterImageView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    final Movie movie = getItem(getAdapterPosition());
                    if (dbNeedsToBeUpdated(movie)) {
                        updateItem(getAdapterPosition(), updateFavoriteMovieData(movie));
                    }
                    openMovieDetailsCallback.openMovieDetails(movie);
                }
            });
        }
    }

    @Override
    protected RecyclerView.ViewHolder createNormalViewHolder(ViewGroup parent) {
        final View listItem = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movie_grid_item, parent, false);
        return new NormalViewHolder(listItem);
    }

    @Override
    protected int getNoDataViewLayoutId() {
        return R.layout.movie_grid_item;
    }

    @Override
    protected void bindData(RecyclerView.ViewHolder holder, int position, Movie data) {
        final NormalViewHolder normalViewHolder = (NormalViewHolder) holder;

        Glide.with(normalViewHolder.posterImageView.getContext())
                .load(data.getPosterUrl())
                .placeholder(R.drawable.image_placeholder)
                .error(R.drawable.image_na)
                .into(normalViewHolder.posterImageView);
    }

    @Override
    protected void loadMoreData() {
        if(!AppUtil.isConnectedToInternet(getContext())) {
            Toast.makeText(
                    getContext(),
                    "Failed to load movies! Please check your internet connection.",
                    Toast.LENGTH_LONG).show();
        } else {
            getNextPage();
        }
    }

    public void getNextPage() {
        if(isLoading()) {
            return;
        }

        setLoading(true);
        updateSortMethod();
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                showProgressBar();
                final Intent msg = new Intent(getContext(), GetService.class);
                msg.putExtra(getContext().getString(R.string.moviedb_sort_by_param), sortMethod);
                msg.putExtra(getContext().getString(R.string.moviedb_page_param), getPage());
                msg.putExtra(GetRequest.class.getCanonicalName(),
                        new GetMovieListRequest());
                getContext().startService(msg);
            }
        });
    }

    public void getFavorites() {
        if(isLoading()) {
            return;
        }

        setLoading(true);
        updateSortMethod();
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                showProgressBar();
                final Intent msg = new Intent(getContext(), GetService.class);
                msg.putExtra(FavoriteMovieListQuery.INTENT_NAME, new FavoriteMovieListQuery());
                msg.putExtra(GetRequest.class.getCanonicalName(),
                        new GetMovieListRequest());
                getContext().startService(msg);
            }
        });
    }

    public boolean sortMethodChanged() {
        final String sortMethod = AppUtil.getSortMethodFromPref(getContext());
        return !sortMethod.equals(this.sortMethod);
    }

    public void updateSortMethod() {
        sortMethod = AppUtil.getSortMethodFromPref(getContext());
    }

    public void setOpenMovieDetailsCallback(OpenMovieDetailsCallback openMovieDetailsCallback) {
        this.openMovieDetailsCallback = openMovieDetailsCallback;
    }

    private boolean dbNeedsToBeUpdated(final Movie movie) {
        Cursor cursor = null;
        try {
            cursor = new FavoriteMovieQuery().execute(getContext(), null,
                    movie.createContentValues(getContext()));
            movie.setFavorite(cursor.moveToNext());
            if(!movie.isFavorite()) {                                                               // doesn't exist in favorites table, not a favorite movie
                return false;
            }
            final int index = cursor.getColumnIndex(getContext().getString(
                    R.string.sql_timestamp_column_name));
            final long timestamp = cursor.getLong(index);
            final long delta = new Date().getTime() - timestamp;
            return SqlUtil.millisToHours(delta) > 24;                                               // only need to update once in a 24 hour time period
        } finally {
            if(cursor != null) cursor.close();
        }
    }

    private Movie updateFavoriteMovieData(Movie movie) {
        final FavoriteMovieUpdate favoriteMovieUpdate = new FavoriteMovieUpdate();
        final String jsonResult = WebUtil.get(favoriteMovieUpdate.buildDownloadUrl(getContext(),
                movie.createContentValues(getContext())));
        try {
            movie = new Movie(getContext(), new JSONObject(jsonResult));
            favoriteMovieUpdate.execute(getContext(), movie.createContentValues(getContext()));
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Unable to update favorite movie data", e);
        }
        return movie;
    }
}
