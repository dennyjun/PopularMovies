package com.example.android.popularmovies.adapters.recyclerview;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.android.popularmovies.R;
import com.example.android.popularmovies.activities.MovieDetailsActivity;
import com.example.android.popularmovies.data.Movie;
import com.example.android.popularmovies.services.GetMovieService;
import com.example.android.popularmovies.utils.AppUtil;

/**
 * Created by Denny on 8/6/2015.
 */
public class MoviePosterAdapter extends BaseRecyclerAdapter<Movie> {
    private String sortMethod = null;

    private final transient Handler runnableHandler = new Handler();
    private final transient Runnable getNextPage = new Runnable() {
        @Override
        public void run() {
            showProgressBar();
            final Intent msg = new Intent(getContext(), GetMovieService.class);
            msg.putExtra(getContext().getString(R.string.moviedb_sort_by_param), sortMethod);
            msg.putExtra(getContext().getString(R.string.moviedb_page_param), getPage());
            getContext().startService(msg);
        }
    };

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
            final Movie movie = getItem(getAdapterPosition());
            final Context context = v.getContext();
            final Intent intent = new Intent(context, MovieDetailsActivity.class);
            intent.putExtra(Intent.EXTRA_STREAM, movie.createContentValues(context));
            v.getContext().startActivity(intent);
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
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Movie movie = getItem(position);
        if(movie == null) {
            return;
        }

        final NormalViewHolder normalViewHolder = (NormalViewHolder) holder;

        Glide.with(normalViewHolder.posterImageView.getContext())
                .load(movie.getPosterUrl())
                .centerCrop()
                .placeholder(R.drawable.image_placeholder)
                .error(R.drawable.image_na)
                .into(normalViewHolder.posterImageView);

        if(!isNoMoreData() && almostAtEndOfList(position)) {
            if(!AppUtil.isConnectedToInternet(getContext())) {
                Toast.makeText(
                        getContext(),
                        "Failed to load movies! Please check your internet connection.",
                        Toast.LENGTH_LONG).show();
            } else {
                getNextPage();
            }
        }
    }

    private boolean almostAtEndOfList(final int position) {
        return position == getItemCount() - 1 && !isLoading();
    }

    public void getNextPage() {
        if(isLoading()) {
            return;
        }

        setLoading(true);
        sortMethod = getSortMethodFromPref();
        runnableHandler.post(getNextPage);
    }

    private String getSortMethodFromPref() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        return prefs.getString(
                getContext().getString(R.string.pref_sort_by_key),
                getContext().getString(R.string.moviedb_popularity_param));
    }

    public boolean sortMethodChanged() {
        final String sortMethod = getSortMethodFromPref();
        return !sortMethod.equals(this.sortMethod);
    }
}
