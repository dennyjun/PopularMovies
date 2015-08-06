package com.example.android.popularmovies.data;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.utils.JsonUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Denny on 8/1/2015.
 */
public class MovieTrailer implements Serializable {
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

    private String buildUrl(final Context c) {
        final Uri.Builder builder = new Uri.Builder();
        builder.scheme(c.getString(R.string.youtube_scheme))
                .authority(c.getString(R.string.youtube_base_url))
                .appendPath(c.getString(R.string.youtube_watch_path))
                .appendQueryParameter(c.getString(R.string.youtube_video_key_param), getKey());
        return builder.build().toString();
    }

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
