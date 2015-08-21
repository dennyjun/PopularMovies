package com.example.android.popularmovies.fragments;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import com.example.android.popularmovies.adapters.recyclerview.MoviePosterAdapter;
import com.example.android.popularmovies.adapters.recyclerview.MovieTrailerAdapter;
import com.example.android.popularmovies.data.Movie;
import com.example.android.popularmovies.data.MovieTrailer;
import com.example.android.popularmovies.database.MovieDbHelper;
import com.example.android.popularmovies.listeners.onclick.FullScreenImagePreviewListener;
import com.example.android.popularmovies.receivers.GetReceiver;
import com.example.android.popularmovies.receivers.ManagedReceiver;
import com.example.android.popularmovies.requests.FavoriteTrailersQuery;
import com.example.android.popularmovies.requests.GetRequest;
import com.example.android.popularmovies.requests.GetRequestResult;
import com.example.android.popularmovies.requests.GetTrailersRequest;
import com.example.android.popularmovies.services.FavoriteService;
import com.example.android.popularmovies.services.GetService;
import com.example.android.popularmovies.utils.AppUtil;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Denny on 8/15/2015.
 * Displays movie details
 */
public class MovieDetailsFragment extends BaseFragment {
    private static final String LOG_TAG = MovieDetailsFragment.class.getSimpleName();
    public static final String TAG = "MOVIE_DETAILS_TAG";

    private static final DecimalFormat ratingFormat = new DecimalFormat("#.0");
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
        Log.v(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        twoPaneMode = isTwoPaneMode();
        loadFavoriteState();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreateView");
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
        loadPoster(rootView);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {                             // Inflate the menu; this adds items to the action bar if it is present.
        Log.v(LOG_TAG, "onCreateOptionsMenu");
        inflater.inflate(R.menu.movie_details, menu);
        final MenuItem menuItem = menu.findItem(R.id.action_favorite);
        final boolean displayToastMsg = false;
        toggleFavoriteIcon(favoriteMovie, menuItem, displayToastMsg);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {                                           // Handle action bar item clicks here. The action bar will
        Log.v(LOG_TAG, "onOptionsItemSelected");                                                    // automatically handle clicks on the Home/Up button, so long
        final int id = item.getItemId();                                                            // as you specify a parent activity in AndroidManifest.xml.
        if(id == R.id.action_favorite) {                                                            // button press means favorite state has changed, update favorite flag
            favoriteMovie = !favoriteMovie;
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
    public List<ManagedReceiver> getReceiversToManage() {
        Log.v(LOG_TAG, "getReceiversToManage");
        final List<ManagedReceiver> managedReceivers = new LinkedList<>();
        final IntentFilter intentFilter = new IntentFilter(GetReceiver.TRAILER_DOWNLOAD_INTENT);
        managedReceivers.add(new ManagedReceiver(new GetTrailerReceiver(), intentFilter));
        return managedReceivers;
    }

    @Override
    public void onInternetConnected() {
        Log.v(LOG_TAG, "onInternetConnected");
        if(needToRetrieveTrailers()) {
            retrieveTrailers(getActivity().getBaseContext());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.v(LOG_TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        saveScrollViewState(outState);
        outState.putParcelable(getString(R.string.movie_trailer_recycler_view_state_key),
                trailersRecyclerView.getLayoutManager().onSaveInstanceState());
        outState.putSerializable(getString(R.string.movie_trailer_adapter_state_key),
                movieTrailerAdapter);
    }

    @Override
    public void onStop() {
        Log.v(LOG_TAG, "onStop");
        super.onStop();
        if(AppUtil.isTabletLayout(getActivity().getBaseContext())) {                            // ignore when in tablet layout, changes are made real time
            return;
        }
        if(favoriteMovie == originalFavoriteState) {                                            // nothing changed, don't do anything
            return;
        }
        originalFavoriteState = favoriteMovie;
        processFavoriteServiceCommand(createFavoriteServiceCommand(favoriteMovie));
    }

    private void loadOverviewTextView(final View rootView) {
        Log.v(LOG_TAG, "loadOverviewTextView");
        final TextView textView = (TextView) rootView.findViewById(R.id.movie_overview_text_view);
        textView.setText(getMovie().getOverview());
    }

    private void loadRatingBar(final View rootView) {
        Log.v(LOG_TAG, "loadRatingBar");
        final RatingBar ratingBar = (RatingBar) rootView.findViewById(R.id.movie_rating_bar);
        ratingBar.setRating((float) getMovie().getVoteAverage() * 0.5f);
    }

    private void loadRatingTextView(final View rootView) {
        Log.v(LOG_TAG, "loadRatingTextView");
        final TextView textView = (TextView) rootView.findViewById(R.id.movie_rating_text_view);
        final String rating = ratingFormat.format(getMovie().getVoteAverage());
        final String voteCount = String.valueOf(getMovie().getVoteCount());
        textView.setText(rating + " - " + voteCount + " votes");
    }

    private void loadTitleTextView(final View rootView) {
        Log.v(LOG_TAG, "loadTitleTextView");
        final TextView textView = (TextView) rootView.findViewById(R.id.title_text_view);
        textView.setText(getMovie().getTitle());
    }

    private void loadReleaseDateTextView(final View rootView) {
        Log.v(LOG_TAG, "loadReleaseDateTextView");
        final TextView textView = (TextView) rootView.findViewById(R.id.year_text_view);
        textView.setText(getMovie().getReleaseDate());
    }

    private void saveScrollViewState(Bundle outState) {
        Log.v(LOG_TAG, "saveScrollViewState");
        final ScrollView scrollView =
                (ScrollView) getActivity().findViewById(R.id.movie_details_scrollview);
        if(scrollView != null) {
            final int scrollPosY = scrollView.getScrollY();
            outState.putInt(getString(R.string.movie_details_scroll_state_key), scrollPosY);
        }
    }

    private void loadScrollViewState(Bundle savedInstanceState, final View rootview) {
        Log.v(LOG_TAG, "loadScrollViewState");
        final ScrollView scrollView =
                (ScrollView) rootview.findViewById(R.id.movie_details_scrollview);
        if(scrollView != null) {
            final int scrollPosY =
                    savedInstanceState.getInt(getString(R.string.movie_details_scroll_state_key));
            scrollView.setScrollY(scrollPosY);
        }
    }

    private void loadFavoriteState() {
        favoriteMovie = getMovie().isFavorite();
        originalFavoriteState = favoriteMovie;                                                      // store original state, need to check when activity is destroyed if need to do work (save / delete fav)
        Log.d(LOG_TAG, "Favorite movie [" + originalFavoriteState + "]");
    }

    private void toggleFavoriteIcon(final boolean favorite, final MenuItem menuItem,
                                    boolean displayToastMsg) {
        Log.d(LOG_TAG, "toggleFavoriteIcon [" + favorite + "]");
        final String msg;
        if(favorite) {
            menuItem.setIcon(R.mipmap.ic_favorite_48dp);
            msg = "Added To Favorites";
            getActivity().setResult(Activity.RESULT_CANCELED);
        } else {
            menuItem.setIcon(R.mipmap.ic_favorite_border_blue_48dp);
            msg = "Removed From Favorites";
            if(!AppUtil.isTabletLayout(getActivity().getBaseContext())) {
                final Intent data = getActivity().getIntent();
                data.putExtra(Intent.EXTRA_STREAM, getMovie());
                getActivity().setResult(Activity.RESULT_OK, data);
            }
        }
        if(displayToastMsg) {
            showFavoriteStatusMsg(getActivity().getBaseContext(), msg);
        }
    }

    private void loadPoster(final View rootView) {
        Log.v(LOG_TAG, "loadPoster");
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
        Log.d(LOG_TAG, "Processing favorite service command [" + command + "]");
        final Context context = getActivity().getBaseContext();
        final Intent intent = new Intent(context, FavoriteService.class);
        intent.putExtra(FavoriteService.INTENT_CMD_PARAM, command);
        intent.putExtra(Intent.EXTRA_STREAM, getMovieParcelable());
        getActivity().startService(intent);
        if(command.equals(FavoriteService.CMD_ADD_FAV)) {
            saveTrailersToDatabase();
        } else if(command.equals(FavoriteService.CMD_REM_FAV)) {
            if (AppUtil.isTabletLayout(context) &&
                    AppUtil.getSortMethodFromPref(context)
                            .equals(getString(R.string.pref_sort_by_favorites))) {
                AppUtil.removeFragment(getFragmentManager(), TAG);
                final RecyclerView recyclerView =
                        (RecyclerView) getActivity().findViewById(R.id.movie_poster_recyclerview);
                final MoviePosterAdapter moviePosterAdapter =
                        (MoviePosterAdapter) recyclerView.getAdapter();
                moviePosterAdapter.removeItem(getMovie());
            }
        }
    }

    private String createFavoriteServiceCommand(final boolean favoriteMovie) {
        Log.v(LOG_TAG, "createFavoriteServiceCommand");
        return favoriteMovie
                ? FavoriteService.CMD_ADD_FAV
                : FavoriteService.CMD_REM_FAV;
    }

    private Intent createSharingIntent(final String subject, final String msg){
        Log.v(LOG_TAG, "createSharingIntent");
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, msg);
        return intent;
    }

    private void saveTrailersToDatabase() {
        Log.i(LOG_TAG, "Saving trailers to the database");
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
        Log.v(LOG_TAG, "isValidTrailer");
        return movieTrailerAdapter.getItemViewType(position) ==
                MovieTrailerAdapter.ViewType.NORMAL.ordinal();
    }

    private boolean needToRetrieveTrailers() {
        final boolean result = !movieTrailerAdapter.isLoading()
                && movieTrailerAdapter.getItemCount() == 0
                && !movieTrailerAdapter.isNoMoreData();
        Log.d(LOG_TAG, "needToRetrieveTrailers [" + result + "]");
        return result;
    }

    private void retrieveTrailers(final Context context) {
        Log.v(LOG_TAG, "retrieveTrailers");
        movieTrailerAdapter.setLoading(true);
        movieTrailerAdapter.showProgressBar();
        final Intent msg = new Intent(context, GetService.class);
        msg.putExtra(context.getString(R.string.moviedb_id_param), getMovie().getId());
        msg.putExtra(GetRequest.class.getCanonicalName(), new GetTrailersRequest());
        if(originalFavoriteState) {
            msg.putExtra(FavoriteTrailersQuery.INTENT_NAME, new FavoriteTrailersQuery());           // tells the service to pull data from the database
        }
        context.startService(msg);
    }

    private void loadDataFromBundle(final Bundle savedInstanceState) {
        Log.v(LOG_TAG, "loadDataFromBundle");
        movieTrailerAdapter = (MovieTrailerAdapter) savedInstanceState
                .getSerializable(getString(R.string.movie_trailer_adapter_state_key));
        trailersRecyclerView.setAdapter(movieTrailerAdapter);
        trailersRecyclerView.getLayoutManager().onRestoreInstanceState(savedInstanceState
                .getParcelable(getString(R.string.movie_trailer_recycler_view_state_key)));
    }

    private RecyclerView createTrailersRecyclerView(final View rootView) {
        Log.v(LOG_TAG, "createTrailersRecyclerView");
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

    private boolean isTwoPaneMode() {
        final boolean result = getResources().getBoolean(R.bool.has_two_panes);
        Log.d(LOG_TAG, "isTwoPaneMode [" + result + "]");
        return result;
    }

    public Parcelable getMovieParcelable() {
        Log.v(LOG_TAG, "getMovieParcelable");
        return getMovie().createContentValues(getActivity().getBaseContext());
    }

    public Movie getMovie() {
        if(movie == null) {
            if(getArguments() != null) {
                Log.i(LOG_TAG, "Load movie from arguments");
                movie = (Movie) getArguments().getSerializable(Intent.EXTRA_STREAM);
            } else {
                Log.i(LOG_TAG, "Load movie from intent");
                movie = (Movie) getActivity().getIntent().getSerializableExtra(Intent.EXTRA_STREAM);
            }
        }
        return movie;
    }

    public void openMovieReviews(final String movieId) {
        Log.d(LOG_TAG, "openMovieReviews [" + movieId + "]");
        final Intent intent =
                new Intent(getActivity().getBaseContext(), MovieReviewsActivity.class);
        final String idParam = getString(R.string.moviedb_id_param);
        intent.putExtra(idParam, movieId);
        startActivity(intent);
    }

    public void shareFirstTrailer(final String movieTitle, final String trailerUrl) {
        Log.d(LOG_TAG, "shareFirstTrailer [" + movieTitle + "]");
        final String msg = "Check out this movie trailer for " + movieTitle + "!\n\n" + trailerUrl;
        final Intent sharingIntent = createSharingIntent(movieTitle, msg);
        startActivity(Intent.createChooser(sharingIntent, "Share Trailer #1 Using:"));
    }

    private boolean firstTrailerAvailable() {
        final boolean result = movieTrailerAdapter != null
                && movieTrailerAdapter.getItem(0) != null;
        Log.d(LOG_TAG, "firstTrailerAvailable [" + result + "]");
        return result;
    }

    private Toast getFavoriteStatus(final Context context) {                                        // http://stackoverflow.com/questions/2755277/android-hide-all-showed-toast-messages
        Log.v(LOG_TAG, "getFavoriteStatus");                                                        // Work around for the toast message queue, setText and show
        if(favoriteStatusToast == null) {
            favoriteStatusToast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
        }
        return favoriteStatusToast;
    }

    private void showFavoriteStatusMsg(final Context context, final String msg) {
        Log.d(LOG_TAG, "showFavoriteStatusMsg [" + msg + "]");
        final Toast toast = getFavoriteStatus(context);
        toast.setText(msg);
        toast.show();
    }

    private class GetTrailerReceiver extends GetReceiver {
        @Override
        public void onDownloadComplete(Context context, Intent intent) {
            Log.v(LOG_TAG, "GetTrailerReceiver.onDownloadComplete");
            try {
                movieTrailerAdapter.hideProgressBar();

                final GetRequestResult<MovieTrailer> result =
                        (GetRequestResult) intent.getSerializableExtra(Intent.EXTRA_STREAM);
                for (final MovieTrailer movieTrailer : result.dataList) {
                    if (!isVideoATrailer(context, movieTrailer)) {
                        continue;
                    }
                    movieTrailerAdapter.addItem(movieTrailer);
                }
                final boolean emptyList = movieTrailerAdapter.getItemCount() == 0;
                if (!AppUtil.isConnectedToInternet(context) && emptyList) {
                    AppUtil.showNoInternetToast(context);
                    return;
                }
                movieTrailerAdapter.setNoMoreData(emptyList);
                if(movieTrailerAdapter.isNoMoreData()) {
                    movieTrailerAdapter.addItem(null);
                }
            } finally {
                movieTrailerAdapter.setLoading(false);
            }
        }

        private boolean isVideoATrailer(final Context context, final MovieTrailer movieTrailer) {
            final boolean result = movieTrailer.getType().equals(
                    context.getString(R.string.moviedb_trailer_type_trailer));
            Log.d(LOG_TAG, "GetTrailerReceiver.isVideoATrailer [" + result + "]");
            return result;
        }
    }
}
