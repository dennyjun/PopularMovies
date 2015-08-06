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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_movie_trailer, container, false);
        trailersRecyclerView =
                (RecyclerView) rootView.findViewById(R.id.movie_trailer_recyclerview);
        trailersRecyclerView.setHasFixedSize(true);
        final LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(
                        rootView.getContext(),
                        LinearLayoutManager.HORIZONTAL,
                        false);
        trailersRecyclerView.setLayoutManager(linearLayoutManager);

        movieTrailerAdapter = new MovieTrailerAdapter(rootView.getContext());
        trailersRecyclerView.setAdapter(movieTrailerAdapter);
        retrieveTrailers(rootView.getContext());

        onConnectReceiver = new OnConnectReceiver() {
            @Override
            public void run() {
            }
        };
        return rootView;
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
}
