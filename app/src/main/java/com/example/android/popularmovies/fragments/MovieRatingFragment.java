package com.example.android.popularmovies.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;

import com.example.android.popularmovies.utils.AppUtil;
import com.example.android.popularmovies.R;

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

        setupRatingBar(rootView, intent);
        setupRatingTextView(rootView, intent);
        setupNumVotesTextView(rootView, intent);

        return rootView;
    }

    private void setupRatingBar(final View rootView, final Intent intent) {
        final RatingBar ratingBar = (RatingBar) rootView.findViewById(R.id.movie_rating_bar);
        final double voteAverage =
                intent.getDoubleExtra(getString(R.string.moviedb_vote_avg_param), 0d);
        ratingBar.setRating((float) voteAverage * 0.5f);
    }

    private void setupRatingTextView(final View rootView, final Intent intent) {
        AppUtil.setTextFromIntentDouble(rootView, intent, ratingFormat,
                R.id.movie_rating_text_view, R.string.moviedb_vote_avg_param);
    }

    private void setupNumVotesTextView(final View rootView, final Intent intent) {
        AppUtil.setTextFromIntentInt(rootView, intent,
                R.id.movie_rating_vote_count, R.string.moviedb_vote_count_param);
    }
}
