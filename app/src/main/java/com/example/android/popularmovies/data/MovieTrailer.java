package com.example.android.popularmovies.data;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.utils.JsonUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Denny on 8/1/2015.
 */
public class MovieTrailer implements ContentValueContainer, JsonDeserializable {
    private static final String LOG_TAG = MovieTrailer.class.getSimpleName();

    private String id;
    private String name;
    private String key;
    private String site;
    private String url;
    private String type;

    public MovieTrailer(final Context c, final JSONObject obj) {
        deserialize(c, obj);
    }

    public MovieTrailer(final Context context, final ContentValues values) {
        readContentValues(context, values);
    }

    @Override
    public void deserialize(final Context c, final JSONObject obj) {
        if(obj == null) {
            return;
        }
        try {
            setId(obj.getString(c.getString(R.string.moviedb_trailer_id_param)));
            setSite(obj.getString(c.getString(R.string.moviedb_trailer_site_param)));
            setKey(obj.getString(c.getString(R.string.moviedb_trailer_key_param)));
            setType(obj.getString(c.getString(R.string.moviedb_trailer_type_param)));
            setName(JsonUtil.getString(obj,
                    c.getString(R.string.moviedb_trailer_name_param)).replaceAll("#", ""));
            setUrl(buildUrl(c));
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Unable to deserialize JSONObject.", e);
        }
    }

    @Override
    public void readContentValues(Context context, ContentValues values) {
        setId(
                (String) values.get(context.getString(R.string.moviedb_trailer_id_param)));
        setSite(
                (String) values.get(context.getString(R.string.moviedb_trailer_site_param)));
        setKey(
                (String) values.get(context.getString(R.string.moviedb_trailer_key_param)));
        setType(
                (String) values.get(context.getString(R.string.moviedb_trailer_type_param)));
        setName(
                (String) values.get(context.getString(R.string.moviedb_trailer_name_param)));
        setUrl(
                (String) values.get(context.getString(
                        R.string.moviedb_trailer_url_sql_column_name)));
    }

    @Override
    public ContentValues createContentValues(final Context context) {
        final ContentValues values = new ContentValues();
        values.put(context.getString(R.string.moviedb_trailer_id_param),
                getId());
        values.put(context.getString(R.string.moviedb_trailer_site_param),
                getSite());
        values.put(context.getString(R.string.moviedb_trailer_key_param),
                getKey());
        values.put(context.getString(R.string.moviedb_trailer_type_param),
                getType());
        values.put(context.getString(R.string.moviedb_trailer_name_param),
                getName());
        values.put(context.getString(R.string.moviedb_trailer_url_sql_column_name),
                getUrl());
        return values;
    }

    private String buildUrl(final Context c) {
        final Uri.Builder builder = new Uri.Builder();
        builder.scheme(c.getString(R.string.youtube_scheme))
                .authority(c.getString(R.string.youtube_base_url))
                .appendPath(c.getString(R.string.youtube_watch_path))
                .appendQueryParameter(c.getString(R.string.youtube_video_key_param), getKey());
        return builder.build().toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
