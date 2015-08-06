package com.example.android.popularmovies.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.data.Movie;
import com.example.android.popularmovies.services.GetMovieService;
import com.example.android.popularmovies.utils.AppUtil;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Denny on 7/15/2015.
 */
public class MovieListAdapter extends BaseAdapter implements Serializable {
    private final transient Context context;
    private final List<Movie> movies = new LinkedList<>();
    private int totalPages = 1;
    private int page = 1;
    private boolean loading = false;
    private String sortMethod = null;
    private transient ProgressBar endOfListProgressBar;

    public MovieListAdapter(final Context context) {
        this.context = context;
    }

    private String getSortMethodFromPref() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_sort_by_key),
                context.getString(R.string.moviedb_popularity_param));
    }

    public boolean sortMethodChanged() {
        final String sortMethod = getSortMethodFromPref();
        return !sortMethod.equals(this.sortMethod);
    }

    public boolean hasMorePages() {
        return page <= totalPages;
    }

    public void getNextPage() {
        if(!hasMorePages()) {
            return;
        }

        loading = true;
        endOfListProgressBar.setVisibility(View.VISIBLE);

        final Intent msg = new Intent(context, GetMovieService.class);
        sortMethod = getSortMethodFromPref();
        msg.putExtra(context.getString(R.string.moviedb_sort_by_param), sortMethod);
        msg.putExtra(context.getString(R.string.moviedb_page_param), page++);
        context.startService(msg);
    }

    /**
     * Must call finalizeDataChange after adding items to update the UI
     * @param movie
     */
    public void addItem(final Movie movie) {
        movies.add(movie);
    }

    public void addItems(final List<Movie> movies) {
        this.movies.addAll(movies);
    }

    public void finalizeDataChange() {
        loading = false;
        notifyDataSetChanged();
        endOfListProgressBar.setVisibility(View.GONE);
    }

    public void clearList() {
        loading = false;
        movies.clear();
        page = 1;
        totalPages = 1;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return movies.size();
    }

    @Override
    public Movie getItem(int position) {
        return movies.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final String posterPath = getItem(position).getPosterPath();
        final String posterUrl = Movie.buildPosterUrl(context, posterPath);

        ViewHolder viewHolder;
        if(convertView == null) {
            convertView =
                    LayoutInflater.from(context).inflate(R.layout.movie_grid_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.imageView = (ImageView) convertView;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Picasso.with(context)
                .load(posterUrl)
                .fit()
                .centerCrop()
                .placeholder(R.drawable.loading_placeholder)
                .error(R.drawable.image_not_available)
                .into(viewHolder.imageView);

        if(almostAtEndOfList(position)) {
            if(!AppUtil.isConnectedToInternet(context)) {
                Toast.makeText(
                        context,
                        "Failed to load movies! Please check your internet connection.",
                        Toast.LENGTH_LONG).show();
            } else {
                getNextPage();
            }
        }
        return convertView;
    }

    private boolean almostAtEndOfList(final int position) {
        return position == getCount() - 1 && !loading;
    }

    private static class ViewHolder {
        public ImageView imageView;
    }

    public void setEndOfListProgressBar(final ProgressBar endOfListProgressBar) {
        this.endOfListProgressBar = endOfListProgressBar;
    }

    public boolean isInitialLoadState() {
        return sortMethod == null && getCount() == 0;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}
