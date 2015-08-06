package com.example.android.popularmovies.adapters.recyclerview;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.popularmovies.asynctasks.GetMovieReviewsTask;
import com.example.android.popularmovies.data.MovieReview;
import com.example.android.popularmovies.R;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Denny on 8/4/2015.
 */
public class MovieReviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements Serializable {
    private static final int VIEW_TYPE_NORMAL = 0;
    private static final int VIEW_TYPE_PROGRESS = 1;
    private static final int VIEW_TYPE_NO_DATA = 2;

    private final transient Context context;
    private List<MovieReview> movieReviews = new LinkedList<>();
    private boolean loading = false;
    private int page = 1;
    private final String movieId;
    private boolean noMoreData = false;

    public static class NormalViewHolder extends RecyclerView.ViewHolder {
        public TextView authorTextView;
        public TextView contentTextView;

        public NormalViewHolder(View listItem) {
            super(listItem);
            authorTextView = (TextView) listItem.findViewById(R.id.movie_review_author_textview);
            contentTextView = (TextView) listItem.findViewById(R.id.movie_review_content_textview);
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

    public MovieReviewAdapter(final Context context, final String movieId) {
        this.context = context;
        this.movieId = movieId;
    }

    @Override
    public int getItemViewType(int position) {
        if(movieReviews.get(position) == null) {
            if(noMoreData) {
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
                    .inflate(R.layout.movie_review_item, parent, false);
            ((CardView) listItem).setUseCompatPadding(true);
            return new NormalViewHolder(listItem);
        } else if(viewType == VIEW_TYPE_PROGRESS) {
            listItem = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.normal_loading_spinner, parent, false);
            return new ProgressBarViewHolder(listItem);
        } else {
            listItem = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.reviews_not_available_textview, parent, false);
            ((CardView) listItem).setUseCompatPadding(true);
            return new NoDataViewHolder(listItem);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final MovieReview movieReview = movieReviews.get(position);
        if(movieReview != null) {
            final NormalViewHolder normalViewHolder = (NormalViewHolder) holder;
            normalViewHolder.authorTextView.setText(movieReview.getAuthor());
            normalViewHolder.contentTextView.setText(movieReview.getContent());
        }

        if(isNoMoreData()) {
            return;
        }
        if(position == getItemCount() - 1 && loading == false) {
            getMoreReviews();
        }
    }

    public void getMoreReviews() {
        loading = true;
        movieReviews.add(null);
        notifyItemInserted(movieReviews.size() - 1);
        new GetMovieReviewsTask(context, MovieReviewAdapter.this)
                .execute(movieId, String.valueOf(page));
    }

    @Override
    public int getItemCount() {
        return movieReviews.size();
    }

    public void addItem(final MovieReview movieReview) {
        movieReviews.add(movieReview);
        notifyItemInserted(movieReviews.size());
    }

    public void addItems(final List<MovieReview> movieReviews) {
        this.movieReviews.addAll(movieReviews);
        notifyDataSetChanged();
    }

    public void removeLast() {
        this.movieReviews.remove(movieReviews.size() - 1);
        notifyItemRemoved(movieReviews.size());
    }

    public void finalizeDataChange() {
        loading = false;
        page++;
    }

    public boolean isNoMoreData() {
        return noMoreData;
    }

    public void setNoMoreData(boolean noMoreData) {
        this.noMoreData = noMoreData;
    }

    public int getPage() {
        return page;
    }

    public boolean isLoading() {
        return loading;
    }
}
