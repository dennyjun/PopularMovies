package com.example.android.popularmovies.requests;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import java.io.Serializable;

/**
 * Created by Denny on 8/20/2015.
 */
public abstract class DbUpdate implements Serializable {
    public static final String INTENT_NAME = DbUpdate.class.getCanonicalName();

    public final int execute(final Context context,
                             final ContentValues contentValues) {
        return context.getContentResolver().update(
                getUri(context),
                contentValues,
                getWhereStatement(context),
                getSelectionArgs(context, contentValues));
    }

    public abstract String buildDownloadUrl(final Context context,
                                            final ContentValues contentValues);
    public abstract Uri getUri(final Context context);
    public abstract String getWhereStatement(final Context context);
    public abstract String[] getSelectionArgs(final Context context,
                                              final ContentValues contentValues);

}
