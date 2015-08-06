package com.example.android.popularmovies.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.adapters.recyclerview.MovieTrailerAdapter;
import com.example.android.popularmovies.asynctasks.GetMovieTrailersTask;
import com.example.android.popularmovies.receivers.OnConnectReceiver;

/**
 *
 */
public class MovieTrailerFragment extends Fragment {
    private OnConnectReceiver onConnectReceiver;
    private RecyclerView trailersRecyclerView;
    private MovieTrailerAdapter movieTrailerAdapter;

    public MovieTrailerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(getString(R.string.movie_trailer_recycler_view_state_key),
                trailersRecyclerView.getLayoutManager().onSaveInstanceState());
        outState.putSerializable(getString(R.string.movie_trailer_adapter_state_key),
                movieTrailerAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_movie_trailer, container, false);
        trailersRecyclerView = createTrailersRecyclerView(rootView);

        if(savedInstanceState != null) {
            loadDataFromBundle(savedInstanceState);
        } else {
            movieTrailerAdapter = new MovieTrailerAdapter(rootView.getContext());
            trailersRecyclerView.setAdapter(movieTrailerAdapter);
            retrieveTrailers(rootView.getContext());
        }

        onConnectReceiver = new OnConnectReceiver() {
            @Override
            public void run() {
            }
        };
        return rootView;
    }

    private RecyclerView createTrailersRecyclerView(final View rootView) {
        final RecyclerView view =
                (RecyclerView) rootView.findViewById(R.id.movie_trailer_recyclerview);
        view.setHasFixedSize(true);
        final LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(
                        rootView.getContext(),
                        LinearLayoutManager.HORIZONTAL,
                        false);
        view.setLayoutManager(linearLayoutManager);
        return view;
    }

    private void retrieveTrailers(final Context context) {
        final String id = getActivity().getIntent()
                .getStringExtra(getString(R.string.moviedb_id_param));
        new GetMovieTrailersTask(context, movieTrailerAdapter).execute(id);
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

    private void loadDataFromBundle(final Bundle savedInstanceState) {
        movieTrailerAdapter = (MovieTrailerAdapter) savedInstanceState
                .getSerializable(getString(R.string.movie_trailer_adapter_state_key));
        trailersRecyclerView.setAdapter(movieTrailerAdapter);
        trailersRecyclerView.getLayoutManager().onRestoreInstanceState(savedInstanceState
                .getParcelable(getString(R.string.movie_trailer_recycler_view_state_key)));
    }
}
