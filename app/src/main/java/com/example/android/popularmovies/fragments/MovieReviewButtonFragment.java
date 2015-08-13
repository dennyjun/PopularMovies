package com.example.android.popularmovies.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dd.CircularProgressButton;
import com.example.android.popularmovies.R;
import com.example.android.popularmovies.activities.MovieReviewsActivity;
import com.example.android.popularmovies.asynctasks.ToggleReadReviewsButtonTask;
import com.example.android.popularmovies.data.Movie;
import com.example.android.popularmovies.receivers.OnConnectReceiver;

/**
 * Created by Denny on 8/6/2015.
 */
public class MovieReviewButtonFragment extends Fragment {
    private OnConnectReceiver onConnectReceiver;

    public MovieReviewButtonFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView =
                inflater.inflate(R.layout.fragment_movie_review_button, container, false);

        onConnectReceiver = new OnConnectReceiver() {
            @Override
            public void run() {
                initOpenReviewButton((CircularProgressButton) rootView);
            }
        };
        return rootView;
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
     * See <a href="https://github.com/dmytrodanylyk/circular-progress-button"> for details
     */
    private void initOpenReviewButton(CircularProgressButton circularProgressButton) {
        circularProgressButton.setIndeterminateProgressMode(true);
        circularProgressButton.setProgress(1);
        circularProgressButton.setVisibility(View.VISIBLE);

        final String idParam = getString(R.string.moviedb_id_param);
        final Movie movie = new Movie(getActivity().getBaseContext(),
                getActivity().getIntent().getParcelableExtra(Intent.EXTRA_STREAM));
        circularProgressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent =
                        new Intent(getActivity(), MovieReviewsActivity.class);
                intent.putExtra(idParam, movie.getId());
                startActivity(intent);
            }
        });

        new ToggleReadReviewsButtonTask(circularProgressButton.getContext(),
                circularProgressButton).execute(movie.getId());
    }
}
