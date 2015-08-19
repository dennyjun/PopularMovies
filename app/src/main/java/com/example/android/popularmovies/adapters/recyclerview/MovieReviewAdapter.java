package com.example.android.popularmovies.adapters.recyclerview;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.asynctasks.GetMovieReviewsTask;
import com.example.android.popularmovies.data.MovieReview;

/**
 * Created by Denny on 8/4/2015.
 */
public class MovieReviewAdapter extends BaseRecyclerAdapter<MovieReview> {
    private final String movieId;

    public static class NormalViewHolder extends RecyclerView.ViewHolder {
        public TextView authorTextView;
        public TextView contentTextView;

        public NormalViewHolder(View listItem) {
            super(listItem);
            authorTextView = (TextView) listItem.findViewById(R.id.movie_review_author_textview);
            contentTextView = (TextView) listItem.findViewById(R.id.movie_review_content_textview);
        }
    }

    public MovieReviewAdapter(final Context context, final String movieId) {
        super(context);
        this.movieId = movieId;
    }

    @Override
    protected RecyclerView.ViewHolder createNormalViewHolder(ViewGroup parent) {
        final View listItem = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movie_review_item, parent, false);
        ((CardView) listItem).setUseCompatPadding(true);
        return new NormalViewHolder(listItem);
    }

    @Override
    protected int getNoDataViewLayoutId() {
        return R.layout.reviews_not_available_textview;
    }

    @Override
    protected void bindData(RecyclerView.ViewHolder holder, int position, MovieReview data) {
        final NormalViewHolder normalViewHolder = (NormalViewHolder) holder;
        normalViewHolder.authorTextView.setText(data.getAuthor());
        normalViewHolder.contentTextView.setText(data.getContent());
    }

    @Override
    protected void loadMoreData() {
        getMoreReviews();
    }

    public void getMoreReviews() {
        setLoading(true);
        showProgressBar();
        new GetMovieReviewsTask(getContext(), MovieReviewAdapter.this)
                .execute(movieId, String.valueOf(getPage()));
    }
}
