package com.example.android.popularmovies.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;

import com.dd.CircularProgressButton;
import com.example.android.popularmovies.asynctasks.ToggleReadReviewsButtonTask;
import com.example.android.popularmovies.data.Movie;
import com.example.android.popularmovies.R;
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

        // https://github.com/dmytrodanylyk/circular-progress-button
        final CircularProgressButton circularProgressButton =
                (CircularProgressButton) findViewById(R.id.movie_review_button);
        circularProgressButton.setIndeterminateProgressMode(true);
        circularProgressButton.setProgress(1);
        circularProgressButton.setVisibility(View.VISIBLE);

        final String idParam = getString(R.string.moviedb_id_param);
        final String movieId = getIntent().getStringExtra(idParam);
        circularProgressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent =
                        new Intent(MovieDetailsActivity.this, MovieReviewsActivity.class);

                intent.putExtra(idParam, movieId);
                startActivity(intent);
            }
        });
        new ToggleReadReviewsButtonTask(this, circularProgressButton).execute(movieId);
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
