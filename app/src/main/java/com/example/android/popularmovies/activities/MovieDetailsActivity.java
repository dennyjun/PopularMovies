package com.example.android.popularmovies.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ScrollView;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.data.Movie;
import com.squareup.picasso.Picasso;


public class MovieDetailsActivity extends AppCompatActivity {

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
        final String posterUrl =
                Movie.buildPosterUrl(this, getIntent().getStringExtra(getString(R.string.moviedb_poster_path_param)));
        final ImageView view = (ImageView) findViewById(R.id.movie_details_poster);

        Picasso.with(this)
                .load(posterUrl)
                .fit()
                .centerCrop()
                .placeholder(R.drawable.loading_placeholder)
                .error(R.drawable.image_not_available)
                .into(view);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.movie_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }
}
