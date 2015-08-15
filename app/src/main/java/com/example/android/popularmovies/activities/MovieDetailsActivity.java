package com.example.android.popularmovies.activities;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.android.popularmovies.R;
import com.example.android.popularmovies.data.Movie;
import com.example.android.popularmovies.providers.MovieContentProvider;
import com.example.android.popularmovies.services.FavoriteService;


public class MovieDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadFavoriteState();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        if(savedInstanceState != null) {
            loadScrollViewState(savedInstanceState);
        }
    }

    private void loadScrollViewState(Bundle savedInstanceState) {
        final int scrollPosY =
                savedInstanceState.getInt(getString(R.string.movie_details_scroll_state_key));
        final ScrollView scrollView =
                (ScrollView) findViewById(R.id.activity_details_scrollview);
        scrollView.setScrollY(scrollPosY);
    }

    private void loadFavoriteState() {
        final Movie movie = getMovieFromIntent(getApplicationContext());                            // super.onCreate will create the fragments so need to store favorite state before that
        final boolean favoriteMovie = checkIfFavoriteMovieFromDb(movie.getId());
        setOriginalFavoriteState(favoriteMovie);                                                    // store original state, need to check when activity is destroyed if need to do work (save / delete fav)
        setFavoriteMovie(favoriteMovie);
    }

    private boolean checkIfFavoriteMovieFromDb(final String movieId) {
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(
                    MovieContentProvider.FAVORITES_CONTENT_URI,
                    new String[]{getString(R.string.moviedb_id_param)},
                    getString(R.string.moviedb_id_param) + "=?",
                    new String[]{movieId},
                    null);
            return cursor.getCount() != 0;
        } finally {
            if(cursor != null) cursor.close();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveScrollViewState(outState);
    }

    private void saveScrollViewState(Bundle outState) {
        final ScrollView scrollView =
                (ScrollView) findViewById(R.id.activity_details_scrollview);
        final int scrollPosY = scrollView.getScrollY();
        outState.putInt(getString(R.string.movie_details_scroll_state_key), scrollPosY);
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadPoster();
    }

    private void loadPoster() {
        final Movie movie = getMovieFromIntent(getApplicationContext());
        final ImageView view = (ImageView) findViewById(R.id.movie_details_poster);

        Glide.with(this)
                .load(movie.getPosterUrl())
                .fitCenter()
                .placeholder(R.drawable.image_placeholder)
                .error(R.drawable.image_na)
                .into(view);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {                                                 // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.movie_details, menu);
        if(isFavoriteMovie()) {
            final MenuItem menuItem = menu.findItem(R.id.action_favorite);
            final boolean displayToastMsg = false;
            displayFavoriteIcon(menuItem, displayToastMsg);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {                                           // Handle action bar item clicks here. The action bar will
        final int id = item.getItemId();                                                            // automatically handle clicks on the Home/Up button, so long
        if(id == R.id.action_favorite) {                                                            // as you specify a parent activity in AndroidManifest.xml.
            final boolean favoriteMovie = !isFavoriteMovie();
            setFavoriteMovie(favoriteMovie);                                                        // button press means favorite state has changed, update favorite flag
            if(favoriteMovie) {
                final boolean displayToastMsg = true;
                displayFavoriteIcon(item, displayToastMsg);
            } else {
                displayNotFavoriteIcon(item);
            }
            return true;
        } else if(id == R.id.action_reviews) {
            final Intent intent = new Intent(this, MovieReviewsActivity.class);
            final String idParam = getString(R.string.moviedb_id_param);
            final Movie movie = getMovieFromIntent(getApplicationContext());
            intent.putExtra(idParam, movie.getId());
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void displayFavoriteIcon(final MenuItem menuItem, boolean displayToastMsg) {
        menuItem.setIcon(R.mipmap.ic_favorite_48dp);
        if(displayToastMsg) {
            Toast.makeText(this, "Added To Favorites", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayNotFavoriteIcon(final MenuItem menuItem) {
        menuItem.setIcon(R.mipmap.ic_favorite_border_blue_48dp);
        Toast.makeText(this, "Removed From Favorites", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        try {
            final boolean favoriteMovie = isFavoriteMovie();
            if(getOriginalFavoriteState() == favoriteMovie) {                                       // nothing changed, don't do anything
                return;
            }
            final String command = favoriteMovie
                    ? FavoriteService.CMD_ADD_FAV
                    : FavoriteService.CMD_REM_FAV;
            processFavoriteServiceCommand(command);
        } finally {
            super.onDestroy();
        }
    }

    private void processFavoriteServiceCommand(final String command) {
        final Intent intent = new Intent(getBaseContext(), FavoriteService.class);
        intent.putExtra(FavoriteService.INTENT_CMD_PARAM, command);
        intent.putExtra(Intent.EXTRA_STREAM,                                                        // need to forward as it includes movie data
                getIntent().getParcelableExtra(Intent.EXTRA_STREAM));
        startService(intent);
    }

    private boolean getOriginalFavoriteState() {
        return getIntent().getBooleanExtra(
                getString(R.string.original_favorite_state_intent_param), false);
    }

    private void setOriginalFavoriteState(final boolean favoriteState) {
        getIntent().putExtra(
                getString(R.string.original_favorite_state_intent_param), favoriteState);
    }

    private boolean isFavoriteMovie() {
        return getIntent().getBooleanExtra(getString(R.string.favorite_state_intent_param), false);
    }

    private void setFavoriteMovie(final boolean favoriteState) {
        getIntent().putExtra(getString(R.string.favorite_state_intent_param), favoriteState);
    }

    private Movie getMovieFromIntent(final Context context) {
        return new Movie(context, getIntent().getParcelableExtra(Intent.EXTRA_STREAM));
    }
}
