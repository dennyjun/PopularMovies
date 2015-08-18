package com.example.android.popularmovies.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.activities.MovieDetailsActivity;
import com.example.android.popularmovies.activities.SettingsActivity;
import com.example.android.popularmovies.adapters.recyclerview.BaseRecyclerAdapter;
import com.example.android.popularmovies.adapters.recyclerview.MoviePosterAdapter;
import com.example.android.popularmovies.asynctasks.GetFavoriteMoviesTask;
import com.example.android.popularmovies.callbacks.OpenMovieDetailsCallback;
import com.example.android.popularmovies.data.Movie;
import com.example.android.popularmovies.receivers.FavoriteReceiver;
import com.example.android.popularmovies.receivers.GetMovieReceiver;
import com.example.android.popularmovies.receivers.OnConnectReceiver;
import com.example.android.popularmovies.utils.AppUtil;


/**
 * A placeholder fragment containing a simple view.
 */
public class MovieGridFragment extends Fragment {
    private boolean twoPaneMode = false;

    private GetMovieReceiver getMovieReceiver;
    private FavoriteReceiver favoriteReceiver;
    private OnConnectReceiver onConnectReceiver;

    private RecyclerView moviesRecyclerView;
    private MoviePosterAdapter moviePosterAdapter;

    public MovieGridFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        twoPaneMode = AppUtil.isTabletLayout(getActivity().getBaseContext());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(getString(R.string.movie_grid_view_state_key),
                moviesRecyclerView.getLayoutManager().onSaveInstanceState());
        outState.putSerializable(getString(R.string.movie_grid_view_adapter_state_key),
                moviePosterAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.movie_grid, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        if(id == R.id.action_sort_by_settings) {
            final Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_movie_grid, container, false);
        moviesRecyclerView = (RecyclerView) rootView.findViewById(R.id.movie_poster_recyclerview);
        moviesRecyclerView.setHasFixedSize(true);
        final int col =
                (getActivity().getResources().getConfiguration().orientation ==
                        Configuration.ORIENTATION_PORTRAIT) || twoPaneMode
                        ? 2
                        : 4;

        final GridLayoutManager gridLayoutManager =
                new GridLayoutManager(getActivity(), col);
        moviePosterAdapter = new MoviePosterAdapter(rootView.getContext());
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {

            @Override
            public int getSpanSize(int position) {
                final int viewType = moviePosterAdapter.getItemViewType(position);
                if (viewType == BaseRecyclerAdapter.ViewType.NORMAL.ordinal()) {
                    return 1;
                } else {
                    return col;
                }
            }
        });
        moviesRecyclerView.setLayoutManager(gridLayoutManager);

        if(savedInstanceState != null) {
            loadDataFromBundle(savedInstanceState);
        } else {
            moviesRecyclerView.setAdapter(moviePosterAdapter);
        }
        moviePosterAdapter.setOpenMovieDetailsCallback(createOpenMovieDetailsCallback());

        getMovieReceiver = new GetMovieReceiver(moviePosterAdapter);
        favoriteReceiver = new FavoriteReceiver(moviePosterAdapter);
        onConnectReceiver = new OnConnectReceiver() {
            @Override
            public void run() {
                if(moviePosterAdapter.sortMethodChanged()) {
                    AppUtil.removeFragment(getFragmentManager(), MovieDetailsFragment.TAG);
                    moviePosterAdapter.resetAdapter();
                    updateMovieGrid();
                } else if(movieGridNeedsUpdating()) {
                    updateMovieGrid();
                }
            }
        };
        return rootView;
    }

    private OpenMovieDetailsCallback createOpenMovieDetailsCallback() {
        return new OpenMovieDetailsCallback() {
            @Override
            public void openMovieDetails(Movie movie) {
                final Context context = getActivity().getBaseContext();
                if(twoPaneMode()) {
                    if(alreadyOpen(movie)) {
                        return;
                    }
                    final MovieDetailsFragment movieDetailsFragment = new MovieDetailsFragment();
                    final Bundle bundle = new Bundle();
                    bundle.putParcelable(Intent.EXTRA_STREAM, movie.createContentValues(context));
                    movieDetailsFragment.setArguments(bundle);
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.movie_details_container,
                                    movieDetailsFragment,
                                    MovieDetailsFragment.TAG)
                            .commit();
                } else {
                    final Intent intent = new Intent(context, MovieDetailsActivity.class);
                    intent.putExtra(Intent.EXTRA_STREAM, movie.createContentValues(context));
                    startActivity(intent);
                }
            }

            @Override
            public boolean alreadyOpen(Movie movie) {
                final FragmentManager fm = getFragmentManager();
                final MovieDetailsFragment movieDetailsFragment =
                        (MovieDetailsFragment) fm.findFragmentByTag(MovieDetailsFragment.TAG);
                return movieDetailsFragment != null
                        && ((ContentValues)movieDetailsFragment.getMovieParcelable())
                        .getAsString(getString(R.string.moviedb_id_param))
                        .equals(movie.getId());
            }

            @Override
            public boolean twoPaneMode() {
                return twoPaneMode;
            }
        };
    }

    /**
     *
     * @param savedInstanceState will be null unless the activity is actually destroyed
     */
    private void loadDataFromBundle(final Bundle savedInstanceState) {
        moviePosterAdapter = (MoviePosterAdapter) savedInstanceState.getSerializable(
                getString(R.string.movie_grid_view_adapter_state_key));
        moviesRecyclerView.setAdapter(moviePosterAdapter);
        moviesRecyclerView.getLayoutManager().onRestoreInstanceState(
                savedInstanceState.getParcelable(
                        getString(R.string.movie_grid_view_state_key)));
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(GetMovieReceiver.class.getCanonicalName());
        getActivity().registerReceiver(getMovieReceiver, intentFilter);
        intentFilter = new IntentFilter(FavoriteReceiver.class.getCanonicalName());
        getActivity().registerReceiver(favoriteReceiver, intentFilter);
        intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        getActivity().registerReceiver(onConnectReceiver, intentFilter);

        if(moviePosterAdapter.sortMethodChanged()) {
            AppUtil.removeFragment(getFragmentManager(), MovieDetailsFragment.TAG);
            moviePosterAdapter.resetAdapter();
            updateMovieGrid();
        } else if(movieGridNeedsUpdating()) {
            updateMovieGrid();
        }

        updateActivityTitle();
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(getMovieReceiver);
        getActivity().unregisterReceiver(favoriteReceiver);
        getActivity().unregisterReceiver(onConnectReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void updateActivityTitle() {
        final String sortMethod = AppUtil.getSortMethodFromPref(getActivity().getBaseContext());
        final String mostPopular = getString(R.string.moviedb_popularity_param);
        final String highestRating = getString(R.string.moviedb_vote_avg_param);
        final String favorites = getString(R.string.pref_sort_by_favorites);
        String title;
        if(sortMethod.equals(mostPopular)) {
            title = getString(R.string.activity_label_popular_movies);
        } else if(sortMethod.equals(highestRating)) {
            title = getString(R.string.activity_label_highest_rated_movies);
        } else if(sortMethod.equals(favorites)) {
            title = getString(R.string.activity_label_favorite_movies);
        } else {
            return;
        }
        getActivity().setTitle(title);
    }

    private boolean movieGridNeedsUpdating() {
        return !moviePosterAdapter.isNoMoreData() && !moviePosterAdapter.isLoading();
    }

    private void updateMovieGrid() {
        final Context context = getActivity().getBaseContext();
        if(getString(R.string.pref_sort_by_favorites).equals(
                AppUtil.getSortMethodFromPref(context))) {
            moviePosterAdapter.setLoading(true);
            moviePosterAdapter.updateSortMethod();
            new GetFavoriteMoviesTask(context, moviePosterAdapter).execute();
            return;
        }

        if(!AppUtil.isConnectedToInternet(context)) {
            Toast.makeText(
                    context,
                    "Failed to load movies! Please check your internet connection.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        moviePosterAdapter.getNextPage();
    }
}
