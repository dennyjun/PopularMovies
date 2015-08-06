package com.example.android.popularmovies.fragments;


import android.app.Fragment;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.adapters.MovieReviewAdapter;
import com.example.android.popularmovies.receivers.OnConnectReceiver;
import com.example.android.popularmovies.utils.AppUtil;

/**
 *
 */
public class MovieReviewFragment extends Fragment {

    private RecyclerView reviewsRecyclerView;
    private MovieReviewAdapter movieReviewAdapter;
    private OnConnectReceiver onConnectReceiver;

    public MovieReviewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("reviewsListViewKey",
                reviewsRecyclerView.getLayoutManager().onSaveInstanceState());
        outState.putSerializable("movieReviewAdapterKey",
                movieReviewAdapter);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_movie_review, container, false);
        reviewsRecyclerView = (RecyclerView) rootView.findViewById(R.id.movie_review_recyclerview);
        reviewsRecyclerView.setHasFixedSize(true);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(rootView.getContext());
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

        onConnectReceiver = new OnConnectReceiver() {
            @Override
            public void run() {
                if(needToCheckForReviews()) {
                    initMovieReviewAdapter();
                }
            }
        };

        return rootView;
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
        return !(movieReviewAdapter.isNoMoreData() && movieReviewAdapter.getItemCount() == 0) && !movieReviewAdapter.isLoading();
    }

    @Override
    public void onStart() {
        super.onStart();
        final IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        getActivity().registerReceiver(onConnectReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(onConnectReceiver);
    }

    /**
     *
     * @param savedInstanceState will be null unless the activity is actually destroyed
     */
    private void loadDataFromBundle(final Bundle savedInstanceState) {
        movieReviewAdapter = (MovieReviewAdapter) savedInstanceState
                .getSerializable("movieReviewAdapterKey");
        reviewsRecyclerView.setAdapter(movieReviewAdapter);
        reviewsRecyclerView.getLayoutManager().onRestoreInstanceState(
                savedInstanceState.getParcelable("reviewsListViewKey"));
    }


}
