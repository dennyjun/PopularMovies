package com.example.android.popularmovies.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.utils.SqlUtil;

/**
 * Created by Denny on 8/7/2015.
 */
public class MovieDbHelper extends SQLiteOpenHelper {
    public static final String DEF_INT = "INTEGER";
    public static final String DEF_STR = "TEXT";
    public static final String DEF_PRI_KEY = "PRIMARY KEY";
    public static final String DEF_FOREIGN_KEY = "FOREIGN KEY(%s) REFERENCES %s(%s)";
    public static final String DEF_NOT_NULL = "NOT NULL";
    public static final String DEF_REAL = "REAL";
    public static final String DEF_BLOB = "BLOB";

    private final Context context;

    public static final String FAVORITES_TABLE_NAME = "favorites";

    public MovieDbHelper(Context context) {
        super(context,
                context.getString(R.string.sql_movie_database_name),
                null,
                Integer.valueOf(context.getString(R.string.sql_movie_database_version)));
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createFavoritesTable(db);
    }

    private void createFavoritesTable(SQLiteDatabase db) {
        db.execSQL(SqlUtil.createTableQuery(FAVORITES_TABLE_NAME,
                SqlUtil.formatColumn(context.getString(R.string.moviedb_id_param),
                        DEF_STR, DEF_PRI_KEY),
                SqlUtil.formatColumn(context.getString(R.string.moviedb_adult_param),
                        DEF_INT),
                SqlUtil.formatColumn(context.getString(R.string.moviedb_original_language_param),
                        DEF_STR),
                SqlUtil.formatColumn(context.getString(R.string.moviedb_original_title_param),
                        DEF_STR),
                SqlUtil.formatColumn(context.getString(R.string.moviedb_overview_param),
                        DEF_STR),
                SqlUtil.formatColumn(context.getString(R.string.moviedb_release_date_param),
                        DEF_STR),
                SqlUtil.formatColumn(context.getString(R.string.moviedb_poster_path_param),
                        DEF_STR),
                SqlUtil.formatColumn(context.getString(R.string.moviedb_title_param),
                        DEF_STR),
                SqlUtil.formatColumn(context.getString(R.string.moviedb_video_param),
                        DEF_INT),
                SqlUtil.formatColumn(context.getString(R.string.moviedb_vote_avg_param),
                        DEF_REAL),
                SqlUtil.formatColumn(context.getString(R.string.moviedb_vote_count_param),
                        DEF_INT),
                SqlUtil.formatColumn(context.getString(R.string.movie_poster_url_param),
                        DEF_STR)
        ));
    }

    private void updateFavoritesTable(SQLiteDatabase db) {
        db.execSQL(SqlUtil.updateTableQuery(FAVORITES_TABLE_NAME));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        updateFavoritesTable(db);
        onCreate(db);
    }
}
