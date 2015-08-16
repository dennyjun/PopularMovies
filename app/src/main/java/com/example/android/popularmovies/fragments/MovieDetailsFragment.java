package com.example.android.popularmovies.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.android.popularmovies.R;
import com.example.android.popularmovies.activities.MovieReviewsActivity;
import com.example.android.popularmovies.adapters.recyclerview.MovieTrailerAdapter;
import com.example.android.popularmovies.asynctasks.GetFavoriteTrailersTask;
import com.example.android.popularmovies.asynctasks.GetMovieTrailersTask;
import com.example.android.popularmovies.data.Movie;
import com.example.android.popularmovies.databases.MovieDbHelper;
import com.example.android.popularmovies.listeners.onclick.FullScreenImagePreviewListener;
import com.example.android.popularmovies.providers.MovieContentProvider;
import com.example.android.popularmovies.receivers.OnConnectReceiver;
import com.example.android.popularmovies.services.FavoriteService;

import java.text.DecimalFormat;

/**
 * Created by Denny on 8/15/2015.
 */
public class MovieDetailsFragment extends Fragment {
    private static final DecimalFormat ratingFormat = new DecimalFormat("#.0");
    private OnConnectReceiver onConnectReceiver;
    private RecyclerView trailersRecyclerView;
    private MovieTrailerAdapter movieTrailerAdapter;

    public MovieDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        loadFavoriteState();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_movie_details, container, false);
        final Intent intent = getActivity().getIntent();
        final Movie movie = new Movie(rootView.getContext(),
                intent.getParcelableExtra(Intent.EXTRA_STREAM));

        setupTitleTextView(rootView, movie);
        setupReleaseDateTextView(rootView, movie);
        setupRatingBar(rootView, movie);
        setupRatingTextView(rootView, movie);
        setupOverviewTextView(rootView, movie);

        trailersRecyclerView = createTrailersRecyclerView(rootView);

        if(savedInstanceState != null) {
            loadDataFromBundle(savedInstanceState);
            loadScrollViewState(savedInstanceState, rootView);
        } else {
            movieTrailerAdapter = new MovieTrailerAdapter(rootView.getContext());
            trailersRecyclerView.setAdapter(movieTrailerAdapter);
            retrieveTrailers(rootView.getContext());
        }

        onConnectReceiver = new OnConnectReceiver() {
            @Override
            public void run() {
                if(needToRetrieveTrailers()) {
                    retrieveTrailers(rootView.getContext());
                }
            }
        };

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {                             // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.movie_details, menu);
        if(isFavoriteMovie()) {
            final MenuItem menuItem = menu.findItem(R.id.action_favorite);
            final boolean displayToastMsg = false;
            displayFavoriteIcon(menuItem, displayToastMsg);
        }
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
            final Intent intent =
                    new Intent(getActivity().getBaseContext(), MovieReviewsActivity.class);
            final String idParam = getString(R.string.moviedb_id_param);
            final Movie movie = getMovieFromIntent(getActivity().getBaseContext());
            intent.putExtra(idParam, movie.getId());
            startActivity(intent);
            return true;
        } else if(id == R.id.action_share) {
            if (movieTrailerAdapter.getItem(0) == null) {
                Toast.makeText(getActivity().getBaseContext(),
                        "Trailer Not Available To Share",
                        Toast.LENGTH_SHORT).show();
                return false;
            }
            final Movie movie = getMovieFromIntent(getActivity().getBaseContext());
            final String subject = movie.getTitle();
            final String msg =
                    "Check out this movie trailer for " + movie.getTitle() + "!\n\n" +
                            movieTrailerAdapter.getItem(0).getUrl();
            final Intent sharingIntent = getDefaultShareIntent(subject, msg);
            startActivity(Intent.createChooser(sharingIntent, "Share Trailer #1 Using:"));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        loadPoster();
        getActivity().registerReceiver(onConnectReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(onConnectReceiver);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveScrollViewState(outState);
        outState.putParcelable(getString(R.string.movie_trailer_recycler_view_state_key),
                trailersRecyclerView.getLayoutManager().onSaveInstanceState());
        outState.putSerializable(getString(R.string.movie_trailer_adapter_state_key),
                movieTrailerAdapter);
    }

    @Override
    public void onDestroy() {
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

    private void setupOverviewTextView(final View rootView, final Movie movie) {
        final TextView textView = (TextView) rootView.findViewById(R.id.movie_overview_text_view);
        textView.setText(movie.getOverview());
    }

    private void setupRatingBar(final View rootView, final Movie movie) {
        final RatingBar ratingBar = (RatingBar) rootView.findViewById(R.id.movie_rating_bar);
        ratingBar.setRating((float) movie.getVoteAverage() * 0.5f);
    }

    private void setupRatingTextView(final View rootView, final Movie movie) {
        final TextView textView = (TextView) rootView.findViewById(R.id.movie_rating_text_view);
        final String rating = ratingFormat.format(movie.getVoteAverage());
        final String voteCount = String.valueOf(movie.getVoteCount());
        textView.setText(rating + " - " + voteCount + " votes");
    }

    private void setupTitleTextView(final View rootView, final  Movie movie) {
        final TextView textView = (TextView) rootView.findViewById(R.id.title_text_view);
        textView.setText(movie.getTitle());
    }

    private void setupReleaseDateTextView(final View rootView,  Movie movie) {
        final TextView textView = (TextView) rootView.findViewById(R.id.year_text_view);
        textView.setText(movie.getReleaseDate());
    }

    private void saveScrollViewState(Bundle outState) {
        final ScrollView scrollView =
                (ScrollView) getActivity().findViewById(R.id.movie_details_scrollview);
        final int scrollPosY = scrollView.getScrollY();
        outState.putInt(getString(R.string.movie_details_scroll_state_key), scrollPosY);
    }

    private void loadScrollViewState(Bundle savedInstanceState, final View rootview) {
        final int scrollPosY =
                savedInstanceState.getInt(getString(R.string.movie_details_scroll_state_key));
        final ScrollView scrollView =
                (ScrollView) rootview.findViewById(R.id.movie_details_scrollview);
        scrollView.setScrollY(scrollPosY);
    }

    private void loadFavoriteState() {
        final Movie movie = getMovieFromIntent(getActivity().getBaseContext());                     // super.onCreate will create the fragments so need to store favorite state before that
        final boolean favoriteMovie = checkIfFavoriteMovieFromDb(movie.getId());
        setOriginalFavoriteState(favoriteMovie);                                                    // store original state, need to check when activity is destroyed if need to do work (save / delete fav)
        setFavoriteMovie(favoriteMovie);
    }

    private boolean checkIfFavoriteMovieFromDb(final String movieId) {
        Cursor cursor = null;
        try {
            cursor = getActivity().getContentResolver().query(
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

    private void displayFavoriteIcon(final MenuItem menuItem, boolean displayToastMsg) {
        menuItem.setIcon(R.mipmap.ic_favorite_48dp);
        if(displayToastMsg) {
            Toast.makeText(getActivity().getBaseContext(),
                    "Added To Favorites", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayNotFavoriteIcon(final MenuItem menuItem) {
        menuItem.setIcon(R.mipmap.ic_favorite_border_blue_48dp);
        Toast.makeText(getActivity().getBaseContext(),
                "Removed From Favorites", Toast.LENGTH_SHORT).show();
    }

    private void loadPoster() {
        final Context context = getActivity().getBaseContext();
        final Movie movie = getMovieFromIntent(context);
        final ImageView view = (ImageView) getActivity().findViewById(R.id.movie_details_poster);
        view.setOnClickListener(
                new FullScreenImagePreviewListener(movie.buildLargePosterUrl(context)));
        view.setClickable(true);

        Glide.with(this)
                .load(movie.getPosterUrl())
                .fitCenter()
                .placeholder(R.drawable.image_placeholder)
                .error(R.drawable.image_na)
                .into(view);
    }

    private void processFavoriteServiceCommand(final String command) {
        final Intent intent = new Intent(getActivity().getBaseContext(), FavoriteService.class);
        intent.putExtra(FavoriteService.INTENT_CMD_PARAM, command);
        intent.putExtra(Intent.EXTRA_STREAM,                                                        // need to forward as it includes movie data
                getActivity().getIntent().getParcelableExtra(Intent.EXTRA_STREAM));
        getActivity().startService(intent);
        if(command.equals(FavoriteService.CMD_ADD_FAV)) {
            saveTrailersToDatabase();
        }
    }

    private boolean getOriginalFavoriteState() {
        return getActivity().getIntent().getBooleanExtra(
                getString(R.string.original_favorite_state_intent_param), false);
    }

    private void setOriginalFavoriteState(final boolean favoriteState) {
        getActivity().getIntent().putExtra(
                getString(R.string.original_favorite_state_intent_param), favoriteState);
    }

    private boolean isFavoriteMovie() {
        return getActivity().getIntent().getBooleanExtra(
                getString(R.string.favorite_state_intent_param), false);
    }

    private void setFavoriteMovie(final boolean favoriteState) {
        getActivity().getIntent().putExtra(
                getString(R.string.favorite_state_intent_param), favoriteState);
    }

    private Movie getMovieFromIntent(final Context context) {
        return new Movie(
                context, getActivity().getIntent().getParcelableExtra(Intent.EXTRA_STREAM));
    }

    private Intent getDefaultShareIntent(final String subject, final String msg){
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, msg);
        return intent;
    }

    private void saveTrailersToDatabase() {
        final Movie movie = getMovieFromIntent(getActivity().getBaseContext());
        final ContentValues[] values = new ContentValues[movieTrailerAdapter.getItemCount()];
        for(int i = 0; i < values.length; ++i) {
            if(!isValidTrailer(i)) {
                continue;
            }
            values[i] = movieTrailerAdapter.getItem(i).createContentValues(getActivity());
            values[i].put(MovieDbHelper.FAVORITES_TABLE_NAME +
                    getActivity().getString(R.string.moviedb_id_param), movie.getId());
        }
        final Intent intent = new Intent(getActivity().getBaseContext(), FavoriteService.class);
        intent.putExtra(FavoriteService.INTENT_CMD_PARAM, FavoriteService.CMD_ADD_TRAILERS);
        intent.putExtra(Intent.EXTRA_STREAM, values);
        getActivity().startService(intent);
    }

    private boolean isValidTrailer(final int position) {
        return movieTrailerAdapter.getItemViewType(position) ==
                MovieTrailerAdapter.ViewType.NORMAL.ordinal();
    }

    private boolean needToRetrieveTrailers() {
        return !movieTrailerAdapter.isLoading()
                && movieTrailerAdapter.getItemCount() == 0
                && !movieTrailerAdapter.isNoMoreData();
    }

    private void retrieveTrailers(final Context context) {
        movieTrailerAdapter.setLoading(true);
        final Movie movie = getMovieFromIntent(getActivity().getBaseContext());
        if(getOriginalFavoriteState()) {
            new GetFavoriteTrailersTask(context, movieTrailerAdapter).execute(movie.getId());
        } else {
            new GetMovieTrailersTask(context, movieTrailerAdapter).execute(movie.getId());
        }
    }

    private void loadDataFromBundle(final Bundle savedInstanceState) {
        movieTrailerAdapter = (MovieTrailerAdapter) savedInstanceState
                .getSerializable(getString(R.string.movie_trailer_adapter_state_key));
        trailersRecyclerView.setAdapter(movieTrailerAdapter);
        trailersRecyclerView.getLayoutManager().onRestoreInstanceState(savedInstanceState
                .getParcelable(getString(R.string.movie_trailer_recycler_view_state_key)));
    }

    private RecyclerView createTrailersRecyclerView(final View rootView) {
        final RecyclerView view =
                (RecyclerView) rootView.findViewById(R.id.movie_trailer_recyclerview);
        view.setHasFixedSize(true);

        final int orientation =
                (getActivity().getResources().getConfiguration().orientation ==
                        Configuration.ORIENTATION_PORTRAIT)
                        ? LinearLayoutManager.VERTICAL
                        : LinearLayoutManager.HORIZONTAL;
        final LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(
                        rootView.getContext(),
                        orientation,
                        false);
        view.setLayoutManager(linearLayoutManager);
        return view;
    }
}
