package com.example.android.popularmovies.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.data.Movie;

import java.text.DecimalFormat;


/**
 *
 */
public class MovieRatingFragment extends Fragment {
    private static final DecimalFormat ratingFormat = new DecimalFormat("#.0");

    public MovieRatingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_movie_rating, container, false);
        final Intent intent = getActivity().getIntent();
        final Movie movie = new Movie(rootView.getContext(),
                intent.getParcelableExtra(Intent.EXTRA_STREAM));

        setupRatingBar(rootView, movie);
        setupRatingTextView(rootView, movie);
        setupNumVotesTextView(rootView, movie);

        return rootView;
    }

    private void setupRatingBar(final View rootView, final Movie movie) {
        final RatingBar ratingBar = (RatingBar) rootView.findViewById(R.id.movie_rating_bar);
        ratingBar.setRating((float) movie.getVoteAverage() * 0.5f);
    }

    private void setupRatingTextView(final View rootView, final Movie movie) {
        final TextView textView = (TextView) rootView.findViewById(R.id.movie_rating_text_view);
        textView.setText(ratingFormat.format(movie.getVoteAverage()));
    }

    private void setupNumVotesTextView(final View rootView, final Movie movie) {
        final TextView textView = (TextView) rootView.findViewById(R.id.movie_rating_vote_count);
        textView.setText(String.valueOf(movie.getVoteCount()));
    }
}
