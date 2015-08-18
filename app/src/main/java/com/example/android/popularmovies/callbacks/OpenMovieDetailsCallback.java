package com.example.android.popularmovies.callbacks;

import com.example.android.popularmovies.data.Movie;

/**
 * Created by Denny on 8/17/2015.
 * Used to open the movie details fragment
 */
public interface OpenMovieDetailsCallback {
    void openMovieDetails(final Movie movie);
    boolean alreadyOpen(final Movie movie);
    boolean twoPaneMode();
}
