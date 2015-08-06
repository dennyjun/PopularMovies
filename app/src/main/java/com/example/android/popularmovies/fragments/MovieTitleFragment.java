package com.example.android.popularmovies.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.utils.AppUtil;


/**
 *
 */
public class MovieTitleFragment extends Fragment {

    public MovieTitleFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_movie_title, container, false);
        final Intent intent = getActivity().getIntent();

        setupTitleTextView(rootView, intent);
        setupReleaseDateTextView(rootView, intent);

        return rootView;
    }

    private void setupTitleTextView(final View rootView, final Intent intent) {
        AppUtil.setTextFromIntentString(rootView, intent,
                R.id.title_text_view, R.string.moviedb_title_param);
    }

    private void setupReleaseDateTextView(final View rootView, final Intent intent) {
        AppUtil.setTextFromIntentString(rootView, intent,
                R.id.year_text_view, R.string.moviedb_release_date_param);
    }
}
