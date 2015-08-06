package com.example.android.popularmovies.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.utils.WebUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Denny on 8/3/2015.
 */
public abstract class GetMovieDataTask<T> extends AsyncTask<String, Void, List<T>> {
    private static final String LOG_TAG = GetMovieDataTask.class.getSimpleName();

    protected final Context context;
    private int totalPages = 0;
    private int totalResults = 0;

    public GetMovieDataTask(final Context context) {
        this.context = context;
    }

    protected final List<T> doInBackground(String... params) {
        final List<T> data = new LinkedList<>();

        final String request = buildRequestUrl(params);
        final String result = WebUtil.get(request);
        try {
            final JSONObject obj = new JSONObject(result);
            if(obj.has(context.getString(R.string.moviedb_data_total_pages_param))) {
                totalPages = obj.getInt(context.getString(R.string.moviedb_data_total_pages_param));
            }
            if(obj.has("total_results")) {
                totalResults = obj.getInt("total_results");
            }
            final JSONArray list = obj.getJSONArray(
                    context.getString(R.string.moviedb_data_results_json_array_param));
            for(int i = 0; i < list.length(); ++i) {
                final JSONObject listObj = list.getJSONObject(i);
                data.add(createDataObject(listObj));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Failed to get movie data using JSON.", e);
        }
        return data;
    }

    protected abstract String buildRequestUrl(String... params);
    protected abstract T createDataObject(JSONObject jsonObject);

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }
}
