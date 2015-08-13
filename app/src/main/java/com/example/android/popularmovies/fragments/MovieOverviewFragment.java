package com.example.android.popularmovies.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.data.Movie;

/**
 * Created by Denny on 7/29/2015.
 */
public class MovieOverviewFragment extends Fragment {

    public MovieOverviewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_movie_overview, container, false);
        final Intent intent = getActivity().getIntent();
        final Movie movie = new Movie(rootView.getContext(),
                intent.getParcelableExtra(Intent.EXTRA_STREAM));

        setupOverviewTextView(rootView, movie);

        return rootView;
    }

    private void setupOverviewTextView(final View rootView, final Movie movie) {
        final TextView textView = (TextView) rootView.findViewById(R.id.movie_overview_text_view);
        textView.setText(movie.getOverview());
    }
}
