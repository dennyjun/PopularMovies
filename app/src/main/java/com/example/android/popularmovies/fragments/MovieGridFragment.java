package com.example.android.popularmovies.fragments;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.example.android.popularmovies.activities.SettingsActivity;
import com.example.android.popularmovies.adapters.recyclerview.BaseRecyclerAdapter;
import com.example.android.popularmovies.adapters.recyclerview.MoviePosterAdapter;
import com.example.android.popularmovies.asynctasks.GetFavoriteMoviesTask;
import com.example.android.popularmovies.receivers.FavoriteReceiver;
import com.example.android.popularmovies.receivers.GetMovieReceiver;
import com.example.android.popularmovies.receivers.OnConnectReceiver;
import com.example.android.popularmovies.utils.AppUtil;


/**
 * A placeholder fragment containing a simple view.
 */
public class MovieGridFragment extends Fragment {

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
                        Configuration.ORIENTATION_PORTRAIT)
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

        getMovieReceiver = new GetMovieReceiver(moviePosterAdapter);
        favoriteReceiver = new FavoriteReceiver(moviePosterAdapter);
        onConnectReceiver = new OnConnectReceiver() {
            @Override
            public void run() {
                if(moviePosterAdapter.sortMethodChanged()) {
                    moviePosterAdapter.resetAdapter();
                    updateMovieGrid();
                } else if(movieGridNeedsUpdating()) {
                    updateMovieGrid();
                }
            }
        };
        return rootView;
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
            moviePosterAdapter.resetAdapter();
            updateMovieGrid();
        } else if(movieGridNeedsUpdating()) {
            updateMovieGrid();
        }
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

    private boolean movieGridNeedsUpdating() {
        return !moviePosterAdapter.isNoMoreData() && !moviePosterAdapter.isLoading();
    }

    private void updateMovieGrid() {
        if(getString(R.string.pref_sort_by_favorites).equals(
                moviePosterAdapter.getSortMethodFromPref())) {
            moviePosterAdapter.setLoading(true);
            moviePosterAdapter.updateSortMethod();
            new GetFavoriteMoviesTask(getActivity().getBaseContext(), moviePosterAdapter).execute();
            return;
        }

        if(!AppUtil.isConnectedToInternet(getActivity())) {
            Toast.makeText(
                    getActivity(),
                    "Failed to load movies! Please check your internet connection.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        moviePosterAdapter.getNextPage();
    }
}
