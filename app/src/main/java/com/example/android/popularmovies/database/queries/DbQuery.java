package com.example.android.popularmovies.database.queries;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import java.io.Serializable;

/**
 * Created by Denny on 8/20/2015.
 */
public abstract class DbQuery implements Serializable {
    public static final String INTENT_NAME = DbQuery.class.getCanonicalName();

    public final Cursor execute(final Context context, final Intent intent) {
        return execute(context, intent, null);
    }

    public final Cursor execute(final Context context, final Intent intent,
                                final ContentValues contentValues) {
        return context.getContentResolver().query(
                getUri(context),
                getProjection(context),
                getSelection(context),
                getSelectionArgs(context, intent, contentValues),
                getSortOrder(context));
    }

    public abstract Uri getUri(final Context context);
    public abstract String[] getProjection(final Context context);
    public abstract String getSelection(final Context context);
    public abstract String[] getSelectionArgs(final Context context, final Intent intent, final ContentValues contentValues);
    public abstract String getSortOrder(final Context context);
}
