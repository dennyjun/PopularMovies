package com.example.android.popularmovies.adapters.recyclerview;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.popularmovies.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Denny on 8/6/2015.
 */
public abstract class BaseRecyclerAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements Serializable {
    private final transient Context context;
    private final List<T> recyclerList = new ArrayList<>(100);
    private boolean loading = false;
    private boolean noMoreData = false;
    private int page = 1;

    public enum ViewType {
        NORMAL,
        PROGRESS,
        NO_DATA
    }

    public static class ProgressBarViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressBarViewHolder(View listItem) {
            super(listItem);
            progressBar = (ProgressBar) listItem.findViewById(R.id.normal_loading_spinner);
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    public static class NoDataViewHolder extends RecyclerView.ViewHolder {
        public TextView noDataTextView;

        public NoDataViewHolder(View listItem) {
            super(listItem);
            noDataTextView = (TextView) listItem.findViewById(R.id.data_not_available_textview);
        }
    }

    public BaseRecyclerAdapter(final Context context) {
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == ViewType.NORMAL.ordinal()) {
            return createNormalViewHolder(parent);
        } else if(viewType == ViewType.PROGRESS.ordinal()) {
            return createProgressViewHolder(parent);
        } else {
            return createNoDataViewHolder(parent);
        }
    }

    protected RecyclerView.ViewHolder createProgressViewHolder(ViewGroup parent) {
        final View listItem = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.normal_loading_spinner, parent, false);
        return new ProgressBarViewHolder(listItem);
    }

    protected abstract RecyclerView.ViewHolder createNormalViewHolder(ViewGroup parent);

    protected RecyclerView.ViewHolder createNoDataViewHolder(ViewGroup parent) {
        final View listItem = LayoutInflater.from(parent.getContext())
                .inflate(getNoDataViewLayoutId(), parent, false);
        ((CardView) listItem).setUseCompatPadding(true);
        return new NoDataViewHolder(listItem);
    }
    protected abstract int getNoDataViewLayoutId();

    @Override
    public int getItemViewType(int position) {
        if(getItem(position) == null) {
            if(noMoreData) {
                return ViewType.NO_DATA.ordinal();
            } else {
                return ViewType.PROGRESS.ordinal();
            }
        } else {
            return ViewType.NORMAL.ordinal();
        }
    }

    @Override
    public int getItemCount() {
        return recyclerList.size();
    }

    public T getItem(final int position) {
        return recyclerList.get(position);
    }

    public void addItem(final T data) {
        recyclerList.add(data);
        notifyItemInserted(recyclerList.size());
    }

    public void addItems(final List<T> dataList) {
        recyclerList.addAll(dataList);
        notifyDataSetChanged();
    }

    /**
     * Must be called before adding items to the list
     */
    public void hideProgressBar() {
        if(!isLoading()) {
            return;
        }
        this.recyclerList.remove(recyclerList.size() - 1);
        notifyItemRemoved(recyclerList.size());
    }

    public void showProgressBar() {
        recyclerList.add(null);
        notifyItemInserted(recyclerList.size() - 1);
    }

    public void resetAdapter() {
        recyclerList.clear();
        loading = false;
        noMoreData = false;
        page = 1;
        notifyDataSetChanged();
    }

    public boolean isLoading() {
        return loading;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    public void setNoMoreData(boolean noMoreData) {
        this.noMoreData = noMoreData;
    }

    public boolean isNoMoreData() {
        return noMoreData;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void incrementPage() {
        page++;
    }

    public Context getContext() {
        return context;
    }
}
