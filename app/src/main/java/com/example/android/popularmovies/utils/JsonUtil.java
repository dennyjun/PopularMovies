package com.example.android.popularmovies.utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Denny on 8/2/2015.
 */
public class JsonUtil {

    /**
     * Fixes UTF-8 encoding issues.
     * Src: http://stackoverflow.com/questions/9069799/android-json-charset-utf-8-problems
     *
     * @param obj
     * @param name
     * @return
     */
    public static String getString(final JSONObject obj, final String name) throws JSONException {
        return StringUtil.toUtf8(obj.getString(name));
    }
}
