package com.example.android.popularmovies.fragments;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.android.popularmovies.activities.SettingsActivity;
import com.example.android.popularmovies.adapters.listview.MovieListAdapter;
import com.example.android.popularmovies.utils.AppUtil;
import com.example.android.popularmovies.receivers.OnConnectReceiver;
import com.example.android.popularmovies.listeners.onitemclick.OpenMovieDetailsListener;
import com.example.android.popularmovies.R;
import com.example.android.popularmovies.receivers.GetMovieReceiver;


/**
 * A placeholder fragment containing a simple view.
 */
public class MovieGridFragment extends Fragment {

    private GetMovieReceiver getMovieReceiver;
    private GridView gridView;
    private MovieListAdapter movieListAdapter;
    private OnConnectReceiver onConnectReceiver;

    public MovieGridFragment() {
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
                gridView.onSaveInstanceState());
        outState.putSerializable(getString(R.string.movie_grid_view_adapter_state_key),
                movieListAdapter);
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

        gridView = (GridView)rootView.findViewById(R.id.movies_grid_view);
        gridView.setOnItemClickListener(new OpenMovieDetailsListener());
        if(savedInstanceState != null) {
            loadDataFromBundle(savedInstanceState);
        } else {
            movieListAdapter = new MovieListAdapter(rootView.getContext());
            gridView.setAdapter(movieListAdapter);
        }
        movieListAdapter.setEndOfListProgressBar(
                (ProgressBar)rootView.findViewById(R.id.loading_more_spinner));
        getMovieReceiver = new GetMovieReceiver(movieListAdapter);
        onConnectReceiver = new OnConnectReceiver() {
            @Override
            public void run() {
                if(movieGridNeedsUpdating()) {
                    updateMovieGrid();
                }
                movieListAdapter.notifyDataSetChanged();
            }
        };
        return rootView;
    }

    /**
     *
     * @param savedInstanceState will be null unless the activity is actually destroyed
     */
    private void loadDataFromBundle(final Bundle savedInstanceState) {
        movieListAdapter = (MovieListAdapter) savedInstanceState.getSerializable(
                getString(R.string.movie_grid_view_adapter_state_key));
        gridView.setAdapter(movieListAdapter);
        gridView.onRestoreInstanceState(
                savedInstanceState.getParcelable(
                        getString(R.string.movie_grid_view_state_key)));
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(getString(R.string.action_get_movie));
        getActivity().registerReceiver(getMovieReceiver, intentFilter);
        intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        getActivity().registerReceiver(onConnectReceiver, intentFilter);
        if(movieGridNeedsUpdating()) {
            updateMovieGrid();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(getMovieReceiver);
        getActivity().unregisterReceiver(onConnectReceiver);
    }

    private boolean movieGridNeedsUpdating() {
        return movieListAdapter.sortMethodChanged() || movieListAdapter.isInitialLoadState();
    }

    private void updateMovieGrid() {
        if(!AppUtil.isConnectedToInternet(getActivity())) {
            Toast.makeText(
                    getActivity(),
                    "Failed to load movies! Please check your internet connection.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        movieListAdapter.clearList();
        movieListAdapter.getNextPage();
    }
}
