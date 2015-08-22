package com.example.android.popularmovies.data;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.utils.JsonUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Denny on 8/2/2015.
 */
public class MovieReview implements ContentValueContainer, JsonDeserializable {
    private static final String LOG_TAG = MovieReview.class.getSimpleName();

    private String id;
    private String author;
    private String content;

    public MovieReview(Context context, JSONObject obj) {
        deserialize(context, obj);
    }

    public MovieReview(Context context, ContentValues contentValues) {
        readContentValues(context, contentValues);
    }

    @Override
    public void deserialize(Context context, JSONObject obj) {
        if(obj == null) {
            return;
        }
        try {
            setId(obj.getString(context.getString(R.string.moviedb_review_id_param)));
            setAuthor(JsonUtil.getString(obj,
                    context.getString(R.string.moviedb_review_author_param)));
            setContent(JsonUtil.getString(obj,
                    context.getString(R.string.moviedb_review_content_param)));
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Unable to deserialize JSONObject.", e);
        }
    }

    @Override
    public void readContentValues(Context context, ContentValues values) {
        setId(values.getAsString(context.getString(R.string.moviedb_review_id_param)));
        setAuthor(values.getAsString(context.getString(R.string.moviedb_review_author_param)));
        setContent(values.getAsString(context.getString(R.string.moviedb_review_content_param)));
    }

    @Override
    public ContentValues createContentValues(Context context) {
        final ContentValues contentValues = new ContentValues();
        contentValues.put(context.getString(R.string.moviedb_review_id_param), getId());
        contentValues.put(context.getString(R.string.moviedb_review_author_param), getAuthor());
        contentValues.put(context.getString(R.string.moviedb_review_content_param), getContent());
        return contentValues;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
