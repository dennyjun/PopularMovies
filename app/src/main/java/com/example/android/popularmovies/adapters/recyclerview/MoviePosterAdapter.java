package com.example.android.popularmovies.adapters.recyclerview;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.android.popularmovies.R;
import com.example.android.popularmovies.callbacks.OpenMovieDetailsCallback;
import com.example.android.popularmovies.data.Movie;
import com.example.android.popularmovies.services.GetMovieService;
import com.example.android.popularmovies.utils.AppUtil;

/**
 * Created by Denny on 8/6/2015.
 * Displays movie posters
 */
public class MoviePosterAdapter extends BaseRecyclerAdapter<Movie> {
    private String sortMethod = null;
    private transient OpenMovieDetailsCallback openMovieDetailsCallback;

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
            openMovieDetailsCallback.openMovieDetails(movie);
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
        updateSortMethod();
        runnableHandler.post(getNextPage);
    }

    public boolean sortMethodChanged() {
        final String sortMethod = AppUtil.getSortMethodFromPref(getContext());
        return !sortMethod.equals(this.sortMethod);
    }

    public String getSortMethod() {
        return sortMethod;
    }

    public void updateSortMethod() {
        sortMethod = AppUtil.getSortMethodFromPref(getContext());
    }

    public void setOpenMovieDetailsCallback(OpenMovieDetailsCallback openMovieDetailsCallback) {
        this.openMovieDetailsCallback = openMovieDetailsCallback;
    }
}
