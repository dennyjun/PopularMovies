package com.example.android.popularmovies.adapters.recyclerview;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.data.MovieTrailer;
import com.example.android.popularmovies.utils.MovieDbUtil;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Denny on 8/6/2015.
 */
public class MovieTrailerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements Serializable {
    private static final int VIEW_TYPE_NORMAL = 0;
    private static final int VIEW_TYPE_PROGRESS = 1;
    private static final int VIEW_TYPE_NO_DATA = 2;

    private final List<MovieTrailer> movieTrailers = new LinkedList<>();
    private final transient Context context;
    private boolean loading = false;
    private boolean noDataFound = false;

    public static class MovieTrailerViewHolder extends RecyclerView.ViewHolder {
        private ImageView previewImageView;
        private TextView labelTextView;

        public MovieTrailerViewHolder(View itemView) {
            super(itemView);
            previewImageView =
                    (ImageView) itemView.findViewById(R.id.movie_trailer_preview_imageview);
            labelTextView = (TextView) itemView.findViewById(R.id.movie_trailer_preview_label);
        }
    }

    public static class ProgressBarViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressBarViewHolder(View listItem) {
            super(listItem);
            progressBar = (ProgressBar) listItem.findViewById(R.id.normal_loading_spinner);
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    public static class NoDataViewHolder extends RecyclerView.ViewHolder {
        public TextView noDataTextView;

        public NoDataViewHolder(View listItem) {
            super(listItem);
            noDataTextView = (TextView) listItem.findViewById(R.id.movie_reviews_not_available_textview);
        }
    }

    public MovieTrailerAdapter(final Context context) {
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        if(movieTrailers.get(position) == null) {
            if(noDataFound) {
                return VIEW_TYPE_NO_DATA;
            } else {
                return VIEW_TYPE_PROGRESS;
            }
        } else {
            return VIEW_TYPE_NORMAL;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View listItem;
        if(viewType == VIEW_TYPE_NORMAL) {
            listItem = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.movie_trailer_item, parent, false);
        } else if(viewType == VIEW_TYPE_PROGRESS) {
            listItem = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.normal_loading_spinner, parent, false);
            return new ProgressBarViewHolder(listItem);
        } else {
            listItem = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.trailers_not_available_textview, parent, false);
            ((CardView) listItem).setUseCompatPadding(true);
            return new NoDataViewHolder(listItem);
        }
        return new MovieTrailerViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(movieTrailers.get(position) == null) {
            return;
        }
        final MovieTrailerViewHolder movieTrailerViewHolder = (MovieTrailerViewHolder) holder;
        final MovieTrailer movieTrailer = movieTrailers.get(position);
        Picasso.with(context)
                .load(MovieDbUtil.buildYouTubeThumbnailUrl(context, movieTrailer.getKey()))
                .fit()
                .centerCrop()
                .placeholder(R.drawable.loading_placeholder)
                .error(R.drawable.image_not_available)
                .into(movieTrailerViewHolder.previewImageView);
        movieTrailerViewHolder.labelTextView.setText(
                context.getString(R.string.moviedb_trailer_thumbnail_prefix) + (position + 1));
    }

    @Override
    public int getItemCount() {
        return movieTrailers.size();
    }

    public void addItem(final MovieTrailer movieTrailer) {
        movieTrailers.add(movieTrailer);
        notifyItemInserted(movieTrailers.size());
    }

    public void addItems(final List<MovieTrailer> movieTrailers) {
        this.movieTrailers.addAll(movieTrailers);
        notifyDataSetChanged();
    }

    public void removeLast() {
        this.movieTrailers.remove(movieTrailers.size() - 1);
        notifyItemRemoved(movieTrailers.size());
    }

    public void setNoDataFound(boolean noDataFound) {
        this.noDataFound = noDataFound;
    }

    public boolean isNoDataFound() {
        return  noDataFound;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    public boolean isLoading() {
        return loading;
    }
}
