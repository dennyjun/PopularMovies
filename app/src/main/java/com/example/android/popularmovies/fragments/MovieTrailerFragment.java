package com.example.android.popularmovies.fragments;

import android.app.Fragment;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.android.popularmovies.asynctasks.GetMovieTrailersTask;
import com.example.android.popularmovies.R;
import com.example.android.popularmovies.receivers.OnConnectReceiver;

/**
 *
 */
public class MovieTrailerFragment extends Fragment {
    private OnConnectReceiver onConnectReceiver;

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
        retrieveTrailers(rootView);

        onConnectReceiver = new OnConnectReceiver() {
            @Override
            public void run() {
                if(needToCheckForTrailers(rootView)) {
                    retrieveTrailers(rootView);
                }
            }
        };
        return rootView;
    }

    /**
     * Determines whether or not trailer retrieval is required.
     * Check for trailers only if
     * 1. Didn't find 0 trailers from previous attempt(s)
     * 2. Currently not attempting to load trailers
     * 3. Didn't already load trailers
     * @param rootView
     * @return
     */
    private boolean needToCheckForTrailers(final View rootView) {
        final View trailersNotAvailTextView = rootView.findViewById(R.id.movie_trailer_na_textview);
        final View progressBar = rootView.findViewById(R.id.loading_trailer_buttons_spinner);
        final LinearLayout trailers =
                (LinearLayout) rootView.findViewById(R.id.movie_trailer_linearlayout);
        return trailersNotAvailTextView.getVisibility() == View.GONE
                && progressBar.getVisibility() == View.GONE
                && trailers.getChildCount() == 1;
    }

    private void retrieveTrailers(final View rootView) {
        final String id = getActivity().getIntent()
                .getStringExtra(getString(R.string.moviedb_id_param));
        new GetMovieTrailersTask(rootView.getContext(),
                (LinearLayout) rootView.findViewById(R.id.movie_trailer_linearlayout)).execute(id);
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
