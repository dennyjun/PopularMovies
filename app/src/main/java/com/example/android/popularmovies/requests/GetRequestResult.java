package com.example.android.popularmovies.requests;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Denny on 8/19/2015.
 */
public class GetRequestResult<T extends Serializable> implements Serializable {
    public final List<T> dataList;
    public final int totalPages;
    public final int totalResults;

    public GetRequestResult(final List<T> dataList, final int totalPages, final int totalResults) {
        this.dataList = dataList;
        this.totalPages = totalPages;
        this.totalResults = totalResults;
    }
}
