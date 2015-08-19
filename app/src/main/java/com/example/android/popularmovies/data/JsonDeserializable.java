package com.example.android.popularmovies.data;

import android.content.Context;

import org.json.JSONObject;

/**
 * Created by Denny on 8/18/2015.
 */
public interface JsonDeserializable {
    void deserialize(final Context c, final JSONObject obj);
}
