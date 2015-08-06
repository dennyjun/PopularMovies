package com.example.android.popularmovies.listeners.onitemclick;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;

import com.example.android.popularmovies.activities.MovieDetailsActivity;
import com.example.android.popularmovies.data.Movie;
import com.example.android.popularmovies.R;

/**
 * Created by Denny on 7/29/2015.
 */
public class OpenMovieDetailsListener implements AdapterView.OnItemClickListener {
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Movie movie = (Movie)parent.getItemAtPosition(position);
        final Context context = view.getContext();
        final Intent intent = new Intent(context, MovieDetailsActivity.class);
        intent.putExtra(context.getString(R.string.moviedb_title_param),
                movie.getTitle());
        intent.putExtra(context.getString(R.string.moviedb_release_date_param),
                movie.getReleaseDate());
        intent.putExtra(context.getString(R.string.moviedb_poster_path_param),
                movie.getPosterPath());
        intent.putExtra(context.getString(R.string.moviedb_backdrop_path_param),
                movie.getBackdropPath());
        intent.putExtra(context.getString(R.string.moviedb_vote_avg_param),
                movie.getVoteAverage());
        intent.putExtra(context.getString(R.string.moviedb_vote_count_param),
                movie.getVoteCount());
        intent.putExtra(context.getString(R.string.moviedb_overview_param),
                movie.getOverview());
        intent.putExtra(context.getString(R.string.moviedb_id_param),
                movie.getId());
        view.getContext().startActivity(intent);
    }
}
