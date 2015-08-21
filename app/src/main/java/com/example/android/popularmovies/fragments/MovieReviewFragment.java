package com.example.android.popularmovies.fragments;


import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.adapters.recyclerview.MovieReviewAdapter;
import com.example.android.popularmovies.receivers.ManagedReceiver;
import com.example.android.popularmovies.utils.AppUtil;

import java.util.List;

/**
 *
 */
public class MovieReviewFragment extends BaseFragment {
    private RecyclerView reviewsRecyclerView;
    private MovieReviewAdapter movieReviewAdapter;

    public MovieReviewFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_movie_review, container, false);
        reviewsRecyclerView = (RecyclerView) rootView.findViewById(R.id.movie_review_recyclerview);
        reviewsRecyclerView.setHasFixedSize(true);
        final LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(rootView.getContext());
        reviewsRecyclerView.setLayoutManager(linearLayoutManager);

        if(savedInstanceState != null) {
            loadDataFromBundle(savedInstanceState);
        } else {

            final String movieId = getActivity().getIntent()
                    .getStringExtra(getString(R.string.moviedb_id_param));
            movieReviewAdapter = new MovieReviewAdapter(rootView.getContext(), movieId);
            reviewsRecyclerView.setAdapter(movieReviewAdapter);
            initMovieReviewAdapter();
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(getString(R.string.movie_review_recycler_view_state_key),
                reviewsRecyclerView.getLayoutManager().onSaveInstanceState());
        outState.putSerializable(getString(R.string.movie_review_adapter_state_key),
                movieReviewAdapter);
    }

    @Override
    public List<ManagedReceiver> getReceiversToManage() {
        // nothing to add, return null
        return null;
    }

    @Override
    public void onInternetConnected() {
        if(needToCheckForReviews()) {
            initMovieReviewAdapter();
        }
    }

    private void initMovieReviewAdapter() {
        if(!AppUtil.isConnectedToInternet(getActivity())) {
            Toast.makeText(
                    getActivity(),
                    "Failed to load reviews! Please check your internet connection.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        movieReviewAdapter.getMoreReviews();
    }

    private boolean needToCheckForReviews() {
        return !(movieReviewAdapter.isNoMoreData()
                && movieReviewAdapter.getItemCount() == 0)
                && !movieReviewAdapter.isLoading();
    }

    /**
     *
     * @param savedInstanceState will be null unless the activity is actually destroyed
     */
    private void loadDataFromBundle(final Bundle savedInstanceState) {
        movieReviewAdapter = (MovieReviewAdapter) savedInstanceState
                .getSerializable(getString(R.string.movie_review_adapter_state_key));
        reviewsRecyclerView.setAdapter(movieReviewAdapter);
        reviewsRecyclerView.getLayoutManager().onRestoreInstanceState(savedInstanceState
                .getParcelable(getString(R.string.movie_review_recycler_view_state_key)));
    }


}
