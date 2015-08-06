package com.example.android.popularmovies.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.popularmovies.utils.AppUtil;
import com.example.android.popularmovies.R;

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

        setupOverviewTextView(rootView, intent);

        return rootView;
    }

    private void setupOverviewTextView(final View rootView, final Intent intent) {
        AppUtil.setTextFromIntentString(rootView, intent,
                R.id.movie_overview_text_view, R.string.moviedb_overview_param);
    }
}
