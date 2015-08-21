package com.example.android.popularmovies.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
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
import com.example.android.popularmovies.callbacks.OpenMovieDetailsCallback;
import com.example.android.popularmovies.data.Movie;
import com.example.android.popularmovies.receivers.GetReceiver;
import com.example.android.popularmovies.receivers.ManagedReceiver;
import com.example.android.popularmovies.requests.GetRequestResult;
import com.example.android.popularmovies.utils.AppUtil;

import java.util.LinkedList;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class MovieGridFragment extends BaseFragment {
    public static final int REMOVE_FAVORITE_REQUEST_CODE = 100;

    private RecyclerView moviesRecyclerView;
    private MoviePosterAdapter moviePosterAdapter;

    public MovieGridFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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
                        Configuration.ORIENTATION_PORTRAIT) ||
                        AppUtil.isTabletLayout(getActivity().getBaseContext())
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
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateActivityTitle();
        onInternetConnected();
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
    public List<ManagedReceiver> getReceiversToManage() {
        final List<ManagedReceiver> managedReceivers = new LinkedList<>();
        final IntentFilter intentFilter = new IntentFilter(GetReceiver.MOVIE_DOWNLOAD_INTENT);
        managedReceivers.add(new ManagedReceiver(new GetMovieListReceiver(), intentFilter));
        return managedReceivers;
    }

    @Override
    public void onInternetConnected() {
        if(moviePosterAdapter.sortMethodChanged()) {
            AppUtil.removeFragment(getFragmentManager(), MovieDetailsFragment.TAG);
            moviePosterAdapter.resetAdapter();
            updateMovieGrid();
        } else if(movieGridNeedsUpdating()) {
            updateMovieGrid();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REMOVE_FAVORITE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            final String sortMethod = AppUtil.getSortMethodFromPref(getActivity().getBaseContext());
            if(sortMethod.equals(getString(R.string.pref_sort_by_favorites))) {
                final Movie movie = (Movie) data.getSerializableExtra(Intent.EXTRA_STREAM);
                moviePosterAdapter.removeItem(movie);
            }
        }
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

    private OpenMovieDetailsCallback createOpenMovieDetailsCallback() {
        return new OpenMovieDetailsCallback() {
            @Override
            public void openMovieDetails(final Movie movie) {
                final Context context = getActivity().getBaseContext();
                if (twoPaneMode()) {
                    if (alreadyOpen(movie)) {
                        return;
                    }
                    final MovieDetailsFragment movieDetailsFragment = new MovieDetailsFragment();
                    final Bundle bundle = new Bundle();
                    bundle.putSerializable(Intent.EXTRA_STREAM, movie);
                    movieDetailsFragment.setArguments(bundle);
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.movie_details_container,
                                    movieDetailsFragment,
                                    MovieDetailsFragment.TAG)
                            .commit();
                } else {
                    final Intent intent = new Intent(context, MovieDetailsActivity.class);
                    intent.putExtra(Intent.EXTRA_STREAM, movie);
                    startActivityForResult(intent, REMOVE_FAVORITE_REQUEST_CODE);
                }
            }

            @Override
            public boolean alreadyOpen(Movie movie) {
                final FragmentManager fm = getFragmentManager();
                final MovieDetailsFragment movieDetailsFragment =
                        (MovieDetailsFragment) fm.findFragmentByTag(MovieDetailsFragment.TAG);
                return movieDetailsFragment != null
                        && movieDetailsFragment.getMovie().getId().equals(movie.getId());
            }

            @Override
            public boolean twoPaneMode() {
                return AppUtil.isTabletLayout(getActivity().getBaseContext());
            }
        };
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
            moviePosterAdapter.getFavorites();
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

    private class GetMovieListReceiver extends GetReceiver {
        @Override
        public void onDownloadComplete(Context context, Intent intent) {
            moviePosterAdapter.hideProgressBar();
            try {
                final GetRequestResult<Movie> result =
                        (GetRequestResult) intent.getSerializableExtra(Intent.EXTRA_STREAM);
                moviePosterAdapter.addItems(result.dataList);
                if(moviePosterAdapter.getItemCount() == 0 &&                                        // only stops loading when favorites is not the sort method
                        !AppUtil.isConnectedToInternet(context)) {
                    AppUtil.showNoInternetToast(context);
                    return;
                }
                moviePosterAdapter.incrementPage();
                moviePosterAdapter.setNoMoreData(moviePosterAdapter.getPage() > result.totalPages);
            } finally {
                moviePosterAdapter.setLoading(false);
            }
        }
    }
}
