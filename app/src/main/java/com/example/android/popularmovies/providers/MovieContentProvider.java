package com.example.android.popularmovies.providers;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.databases.MovieDbHelper;

/**
 * Created by Denny on 8/7/2015.
 */
public class MovieContentProvider extends ContentProvider {
    private static final String AUTHORITY = MovieContentProvider.class.getCanonicalName();
    private static final String FAVORITES_BASE_PATH = "favorites";
    private static final String TRAILERS_BASE_PATH = "trailers";

    public static final Uri FAVORITES_CONTENT_URI =
            Uri.parse("content://" + AUTHORITY + "/" + FAVORITES_BASE_PATH);
    public static final Uri TRAILERS_CONTENT_URI =
            Uri.parse("content://" + AUTHORITY + "/" + TRAILERS_BASE_PATH);

    private static final int URI_CODE_FAVORITES = 100;
    private static final int URI_CODE_FAVORITE_ID = 110;
    private static final int URI_CODE_TRAILERS = 120;
    private static final int URI_CODE_TRAILER_ID = 130;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(AUTHORITY, FAVORITES_BASE_PATH, URI_CODE_FAVORITES);                      // returns all favorites
        uriMatcher.addURI(AUTHORITY, FAVORITES_BASE_PATH + "/#", URI_CODE_FAVORITE_ID);             // returns a single favorite movie
        uriMatcher.addURI(AUTHORITY, TRAILERS_BASE_PATH, URI_CODE_TRAILERS);
        uriMatcher.addURI(AUTHORITY, TRAILERS_BASE_PATH + "/#", URI_CODE_TRAILER_ID);
    }

    private MovieDbHelper movieDbHelper;

    @Override
    public boolean onCreate() {
        movieDbHelper = new MovieDbHelper(getContext());
        return movieDbHelper.getWritableDatabase() != null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(getTableName(uri));
        final String whereClause = buildWhereClause(uri);
        if(!whereClause.isEmpty()) {
            queryBuilder.appendWhere(whereClause);
        }
        final Cursor cursor = queryBuilder.query(
                movieDbHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null, null,
                sortOrder
        );
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        final int uriType = uriMatcher.match(uri);
        switch (uriType) {
            case URI_CODE_FAVORITES:
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + FAVORITES_BASE_PATH;
            case URI_CODE_FAVORITE_ID:
                return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + FAVORITES_BASE_PATH;
            case URI_CODE_TRAILERS:
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + TRAILERS_BASE_PATH;
            case URI_CODE_TRAILER_ID:
                return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + TRAILERS_BASE_PATH;
            default: throw new IllegalArgumentException("Invalid URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final long id = movieDbHelper.getWritableDatabase().insertWithOnConflict(
                getTableName(uri),
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final String whereClause = buildWhereClause(uri, selection);
        return movieDbHelper.getWritableDatabase().delete(
                getTableName(uri),
                whereClause,
                selectionArgs
        );
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final String whereClause = buildWhereClause(uri, selection);
        return movieDbHelper.getWritableDatabase().update(
                getTableName(uri),
                values,
                whereClause,
                selectionArgs
        );
    }

    private String getTableName(final Uri uri) {
        final int uriType = uriMatcher.match(uri);
        switch (uriType) {
            case URI_CODE_FAVORITES:
                return MovieDbHelper.FAVORITES_TABLE_NAME;
            case URI_CODE_TRAILERS:
                return MovieDbHelper.TRAILERS_TABLE_NAME;
            default: throw new IllegalArgumentException("Invalid URI: " + uri);
        }
    }

    private String buildWhereClause(Uri uri) {
        return buildWhereClause(uri, "");
    }

    private String buildWhereClause(Uri uri, String selection) {
        String whereClause = selection;
        final int uriType = uriMatcher.match(uri);
        switch (uriType) {
            case URI_CODE_TRAILERS:
            case URI_CODE_FAVORITES:
                break;
            case URI_CODE_TRAILER_ID:
            case URI_CODE_FAVORITE_ID:
                final String idName = getContext().getString(R.string.moviedb_id_param);
                whereClause = idName + " = " + uri.getLastPathSegment();
                if (!selection.isEmpty()) {
                    whereClause += " AND " + selection;
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid URI: " + uri);
        }
        return whereClause;
    }
}
