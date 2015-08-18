package com.example.android.popularmovies.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Parcelable;
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
import com.example.android.popularmovies.utils.AppUtil;

import java.text.DecimalFormat;

/**
 * Created by Denny on 8/15/2015.
 * Displays movie details
 */
public class MovieDetailsFragment extends Fragment {
    public static final String TAG = "MOVIE_DETAILS_TAG";

    private static final DecimalFormat ratingFormat = new DecimalFormat("#.0");
    private OnConnectReceiver onConnectReceiver;
    private RecyclerView trailersRecyclerView;
    private MovieTrailerAdapter movieTrailerAdapter;

    private Movie movie;
    private boolean twoPaneMode = false;
    private boolean favoriteMovie;
    private boolean originalFavoriteState;
    private Toast favoriteStatusToast;

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
        loadTitleTextView(rootView);
        loadReleaseDateTextView(rootView);
        loadRatingBar(rootView);
        loadRatingTextView(rootView);
        loadOverviewTextView(rootView);

        trailersRecyclerView = createTrailersRecyclerView(rootView);

        if(savedInstanceState != null) {
            loadDataFromBundle(savedInstanceState);
            loadScrollViewState(savedInstanceState, rootView);
        } else {
            movieTrailerAdapter = new MovieTrailerAdapter(rootView.getContext());
            trailersRecyclerView.setAdapter(movieTrailerAdapter);
            retrieveTrailers(rootView.getContext());
        }

        loadOnConnectReceiver(rootView.getContext());
        loadPoster(rootView);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {                             // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.movie_details, menu);
        final MenuItem menuItem = menu.findItem(R.id.action_favorite);
        final boolean displayToastMsg = false;
        toggleFavoriteIcon(favoriteMovie, menuItem, displayToastMsg);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {                                           // Handle action bar item clicks here. The action bar will
        final int id = item.getItemId();                                                            // automatically handle clicks on the Home/Up button, so long
        if(id == R.id.action_favorite) {                                                            // as you specify a parent activity in AndroidManifest.xml.
            favoriteMovie = !favoriteMovie;                                                         // button press means favorite state has changed, update favorite flag
            final boolean displayToastMsg = true;
            toggleFavoriteIcon(favoriteMovie, item, displayToastMsg);
            if (AppUtil.isTabletLayout(getActivity().getBaseContext())) {                           // Only make changes to favorites right away with tablet view,
                processFavoriteServiceCommand(createFavoriteServiceCommand(favoriteMovie));         // phone view will make changes ondestroy to minimize unnecessary calls
            }
            return true;
        } else if(id == R.id.action_reviews) {
            openMovieReviews(getMovie().getId());
            return true;
        } else if(id == R.id.action_share) {
            if(firstTrailerAvailable()) {
                shareFirstTrailer(getMovie().getTitle(), movieTrailerAdapter.getItem(0).getUrl());
            } else {
                Toast.makeText(getActivity().getBaseContext(),
                        "Trailer Not Available To Share",
                        Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
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
            if(AppUtil.isTabletLayout(getActivity().getBaseContext())) {                            // ignore when in tablet layout, changes are made real time
               return;
            }
            if(favoriteMovie == originalFavoriteState) {                                            // nothing changed, don't do anything
                return;
            }
            processFavoriteServiceCommand(createFavoriteServiceCommand(favoriteMovie));
        } finally {
            super.onDestroy();
        }
    }

    private void loadOverviewTextView(final View rootView) {
        final TextView textView = (TextView) rootView.findViewById(R.id.movie_overview_text_view);
        textView.setText(getMovie().getOverview());
    }

    private void loadRatingBar(final View rootView) {
        final RatingBar ratingBar = (RatingBar) rootView.findViewById(R.id.movie_rating_bar);
        ratingBar.setRating((float) getMovie().getVoteAverage() * 0.5f);
    }

    private void loadRatingTextView(final View rootView) {
        final TextView textView = (TextView) rootView.findViewById(R.id.movie_rating_text_view);
        final String rating = ratingFormat.format(getMovie().getVoteAverage());
        final String voteCount = String.valueOf(getMovie().getVoteCount());
        textView.setText(rating + " - " + voteCount + " votes");
    }

    private void loadTitleTextView(final View rootView) {
        final TextView textView = (TextView) rootView.findViewById(R.id.title_text_view);
        textView.setText(getMovie().getTitle());
    }

    private void loadReleaseDateTextView(final View rootView) {
        final TextView textView = (TextView) rootView.findViewById(R.id.year_text_view);
        textView.setText(getMovie().getReleaseDate());
    }

    private void loadOnConnectReceiver(final Context context) {
        onConnectReceiver = new OnConnectReceiver() {
            @Override
            public void run() {
                if(needToRetrieveTrailers()) {
                    retrieveTrailers(context);
                }
            }
        };
    }

    private void saveScrollViewState(Bundle outState) {
        final ScrollView scrollView =
                (ScrollView) getActivity().findViewById(R.id.movie_details_scrollview);
        if(scrollView != null) {
            final int scrollPosY = scrollView.getScrollY();
            outState.putInt(getString(R.string.movie_details_scroll_state_key), scrollPosY);
        }
    }

    private void loadScrollViewState(Bundle savedInstanceState, final View rootview) {
        final ScrollView scrollView =
                (ScrollView) rootview.findViewById(R.id.movie_details_scrollview);
        if(scrollView != null) {
            final int scrollPosY =
                    savedInstanceState.getInt(getString(R.string.movie_details_scroll_state_key));
            scrollView.setScrollY(scrollPosY);
        }
    }

    private void loadFavoriteState() {
        favoriteMovie = checkIfFavoriteMovieFromDb(getMovie().getId());
        originalFavoriteState = favoriteMovie;                                                      // store original state, need to check when activity is destroyed if need to do work (save / delete fav)
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

    private void toggleFavoriteIcon(final boolean favorite, final MenuItem menuItem,
                                    boolean displayToastMsg) {
        final String msg;
        if(favorite) {
            menuItem.setIcon(R.mipmap.ic_favorite_48dp);
            msg = "Added To Favorites";
        } else {
            menuItem.setIcon(R.mipmap.ic_favorite_border_blue_48dp);
            msg = "Removed From Favorites";
        }
        if(displayToastMsg) {
            showFavoriteStatusMsg(getActivity().getBaseContext(), msg);
        }
    }

    private void loadPoster(final View rootView) {
        final ImageView view = (ImageView) rootView.findViewById(R.id.movie_details_poster);
        view.setOnClickListener(new FullScreenImagePreviewListener(
                getMovie().buildLargePosterUrl(rootView.getContext())));
        view.setClickable(true);

        Glide.with(this)
                .load(getMovie().getPosterUrl())
                .fitCenter()
                .placeholder(R.drawable.image_placeholder)
                .error(R.drawable.image_na)
                .into(view);
    }

    private void processFavoriteServiceCommand(final String command) {
        final Context context = getActivity().getBaseContext();
        final Intent intent = new Intent(context, FavoriteService.class);
        intent.putExtra(FavoriteService.INTENT_CMD_PARAM, command);
        intent.putExtra(Intent.EXTRA_STREAM, getMovieParcelable());                                 // need to forward as it includes movie data
        getActivity().startService(intent);
        if(command.equals(FavoriteService.CMD_ADD_FAV)) {
            saveTrailersToDatabase();
        } else if(command.equals(FavoriteService.CMD_REM_FAV)) {
            if (AppUtil.isTabletLayout(context) &&
                    AppUtil.getSortMethodFromPref(context)
                            .equals(getString(R.string.pref_sort_by_favorites))) {
                AppUtil.removeFragment(getFragmentManager(), TAG);
            }
        }
    }

    private String createFavoriteServiceCommand(final boolean favoriteMovie) {
        return favoriteMovie
                ? FavoriteService.CMD_ADD_FAV
                : FavoriteService.CMD_REM_FAV;
    }

    private Intent createSharingIntent(final String subject, final String msg){
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, msg);
        return intent;
    }

    private void saveTrailersToDatabase() {
        final ContentValues[] values = new ContentValues[movieTrailerAdapter.getItemCount()];
        for(int i = 0; i < values.length; ++i) {
            if(!isValidTrailer(i)) {
                continue;
            }
            values[i] = movieTrailerAdapter.getItem(i).createContentValues(getActivity());
            values[i].put(MovieDbHelper.FAVORITES_TABLE_NAME +
                    getActivity().getString(R.string.moviedb_id_param), getMovie().getId());
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
        if(originalFavoriteState) {
            new GetFavoriteTrailersTask(context, movieTrailerAdapter).execute(getMovie().getId());
        } else {
            new GetMovieTrailersTask(context, movieTrailerAdapter).execute(getMovie().getId());
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
                        Configuration.ORIENTATION_PORTRAIT || twoPaneMode)
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

    public Parcelable getMovieParcelable() {
        twoPaneMode = isTwoPaneMode();
        if(getArguments() != null) {
            return getArguments().getParcelable(Intent.EXTRA_STREAM);
        } else {
            return getActivity().getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
        }
    }

    private boolean isTwoPaneMode() {
        return getResources().getBoolean(R.bool.has_two_panes);
    }

    public Movie getMovie() {
        if(movie == null) {
            movie = new Movie(getActivity().getBaseContext(), getMovieParcelable());
        }
        return movie;
    }

    public void openMovieReviews(final String id) {
        final Intent intent =
                new Intent(getActivity().getBaseContext(), MovieReviewsActivity.class);
        final String idParam = getString(R.string.moviedb_id_param);
        intent.putExtra(idParam, id);
        startActivity(intent);
    }

    public void shareFirstTrailer(final String movieTitle, final String trailerUrl) {
        final String msg = "Check out this movie trailer for " + movieTitle + "!\n\n" + trailerUrl;
        final Intent sharingIntent = createSharingIntent(movieTitle, msg);
        startActivity(Intent.createChooser(sharingIntent, "Share Trailer #1 Using:"));
    }

    private boolean firstTrailerAvailable() {
        return movieTrailerAdapter != null && movieTrailerAdapter.getItem(0) != null;
    }

    private Toast getFavoriteStatus(final Context context) {                                        // http://stackoverflow.com/questions/2755277/android-hide-all-showed-toast-messages
        if(favoriteStatusToast == null) {                                                           // Work around for the toast message queue, setText and show
            favoriteStatusToast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
        }
        return favoriteStatusToast;
    }

    private void showFavoriteStatusMsg(final Context context, final String msg) {
        final Toast toast = getFavoriteStatus(context);
        toast.setText(msg);
        toast.show();
    }
}
