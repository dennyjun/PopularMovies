package com.example.android.popularmovies.requests;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.util.Log;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.data.ContentValueContainer;
import com.example.android.popularmovies.database.DbQuery;
import com.example.android.popularmovies.database.DbUpdate;
import com.example.android.popularmovies.utils.AppUtil;
import com.example.android.popularmovies.utils.WebUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Denny on 8/19/2015.
 * Use with GetService to get data and broadcast the results
 */
public abstract class GetRequest<T extends ContentValueContainer> implements Serializable {
    private static final String LOG_TAG = GetRequest.class.getSimpleName();

    /**
     *
     * @param context
     * @param intent
     * @return JSON result string
     */
    public GetRequestResult handleServiceIntent(Context context, Intent intent) {
        try {
            final boolean dbRequest = intent.hasExtra(DbQuery.INTENT_NAME);
            if (dbRequest) {
                return getDataFromDatabase(context, intent);
            }
            final String jsonResult = WebUtil.get(buildDownloadUrl(context, intent));
            return extractDataFromJson(context, jsonResult);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Failed to get movie data using JSON.", e);
            return new GetRequestResult(new LinkedList(), 0, 0);
        }
    }

    public GetRequestResult extractDataFromJson(final Context context,
                                                final String jsonResult) throws JSONException {
        final List<T> data = new LinkedList<>();
        int totalPages = 0;
        int totalResults = 0;

        final JSONObject obj = new JSONObject(jsonResult);
        if(obj.has(context.getString(R.string.moviedb_data_total_pages_param))) {
            totalPages = obj.getInt(context.getString(R.string.moviedb_data_total_pages_param));
        }
        if(obj.has(context.getString(R.string.moviedb_data_total_results_param))) {
            totalResults =
                    obj.getInt(context.getString(R.string.moviedb_data_total_results_param));
        }
        final JSONArray list = obj.getJSONArray(
                context.getString(R.string.moviedb_data_results_json_array_param));
        for(int i = 0; i < list.length(); ++i) {
            final JSONObject listObj = list.getJSONObject(i);
            data.add(createDataObject(context, listObj));
        }

        return new GetRequestResult(data, totalPages, totalResults);
    }

    public GetRequestResult getDataFromDatabase(Context context,
                                                Intent intent) throws JSONException {
        final List<T> data = new LinkedList<>();
        final DbQuery query = (DbQuery) intent.getSerializableExtra(DbQuery.INTENT_NAME);
        Cursor cursor = null;
        try {
            cursor = query.execute(context, intent);

            while (cursor.moveToNext()) {
                final ContentValues contentValues = new ContentValues();
                DatabaseUtils.cursorRowToContentValues(cursor, contentValues);
                if (AppUtil.isConnectedToInternet(context)
                        && intent.hasExtra(DbUpdate.INTENT_NAME)) {                                 // Update favorite movie data in the database whenever there's internet and update is requested
                    data.add(update(context, intent, contentValues));
                } else {
                    data.add(createDataObject(context, contentValues));
                }
            }
        } finally {
            if(cursor != null) cursor.close();
        }
        return new GetRequestResult(data, 0, 0);
    }

    private T update(final Context context, final Intent intent,
                     final ContentValues contentValues) throws JSONException {
        final DbUpdate update = (DbUpdate) intent.getSerializableExtra(DbUpdate.INTENT_NAME);

        final String jsonResult = WebUtil.get(update.buildDownloadUrl(context, contentValues));
        final JSONObject obj = new JSONObject(jsonResult);
        final T dataObject = createDataObject(context, obj);

        update.execute(context, dataObject.createContentValues(context));
        return dataObject;
    }

    public abstract String buildDownloadUrl(Context context, Intent intent);
    protected abstract T createDataObject(Context context, JSONObject jsonObject);
    protected abstract T createDataObject(Context context, ContentValues contentValues);
    public abstract Intent getBroadcastReceiverIntent();
}
