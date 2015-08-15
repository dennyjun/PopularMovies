package com.example.android.popularmovies.utils;

/**
 * Created by Denny on 8/7/2015.
 */
public class SqlUtil {
    public static String createTableQuery(final String... strings) {
        final StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ").append(strings[0]).append(" (");
        for(int i = 1; i < strings.length; ++i) {
            sb.append(strings[i]);
            if(i + 1 != strings.length) {
                sb.append(", ");
            }
        }
        sb.append(");");
        return sb.toString();
    }

    public static String formatColumn(final String... strings) {
        final StringBuilder sb = new StringBuilder();
        sb.append(strings[0]);
        for(int i = 1; i < strings.length; ++i) {
            sb.append(" ").append(strings[i]);
        }
        return sb.toString();
    }

    public static String updateTableQuery(final String tableName) {
        return "DROP TABLE IF EXISTS " + tableName;
    }
}
