package com.example.android.popularmovies.activities;

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

    private boolean favoriteMovie = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        if(savedInstanceState != null) {
            final int scrollPosY =
                    savedInstanceState.getInt(getString(R.string.movie_details_scroll_state_key));
            final ScrollView scrollView =
                    (ScrollView) findViewById(R.id.activity_details_scrollview);
            scrollView.setScrollY(scrollPosY);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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
        final Movie movie = new Movie(getApplicationContext(),
                getIntent().getParcelableExtra(Intent.EXTRA_STREAM));
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
        final Movie movie = new Movie(getApplicationContext(),
                getIntent().getParcelableExtra(Intent.EXTRA_STREAM));
        final Cursor cursor = getContentResolver().query(
                MovieContentProvider.CONTENT_URI,
                new String[]{getString(R.string.moviedb_id_param)},
                getString(R.string.moviedb_id_param) + "=?",
                new String[]{movie.getId()},
                null);
        favoriteMovie = cursor.getCount() != 0;
        if(favoriteMovie) {
            final MenuItem menuItem = menu.findItem(R.id.action_favorite);
            menuItem.setIcon(R.mipmap.ic_favorite_48dp);
        }
        getIntent().putExtra(Intent.EXTRA_TEXT, favoriteMovie);                                     // store original state, need to check when activity
        return true;                                                                                // is destroyed if need to do work (save / delete fav)
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {                                           // Handle action bar item clicks here. The action bar will
        int id = item.getItemId();                                                                  // automatically handle clicks on the Home/Up button, so long
        if(item.getItemId() == R.id.action_favorite) {                                              // as you specify a parent activity in AndroidManifest.xml.
            favoriteMovie = !favoriteMovie;
            if(favoriteMovie) {
                item.setIcon(R.mipmap.ic_favorite_48dp);
                Toast.makeText(this, "Added To Favorites", Toast.LENGTH_SHORT).show();
            } else {
                item.setIcon(R.mipmap.ic_favorite_border_blue_48dp);
                Toast.makeText(this, "Removed From Favorites", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        try {
            if(getIntent().getBooleanExtra(Intent.EXTRA_TEXT, favoriteMovie) == favoriteMovie) {    // nothing changed, don't do anything
                return;
            }
            final Intent intent = new Intent(getBaseContext(), FavoriteService.class);
            final String command = favoriteMovie ? "add" : "remove";
            intent.putExtra("command", command);
            intent.putExtra(Intent.EXTRA_STREAM,
                    getIntent().getParcelableExtra(Intent.EXTRA_STREAM));
            startService(intent);
        } finally {
            super.onDestroy();
        }
    }
}
