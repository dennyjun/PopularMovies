package com.example.android.popularmovies.adapters.recyclerview;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.popularmovies.R;
import com.example.android.popularmovies.data.MovieTrailer;
import com.example.android.popularmovies.utils.MovieDbUtil;

/**
 * Created by Denny on 8/6/2015.
 */
public class MovieTrailerAdapter extends BaseRecyclerAdapter<MovieTrailer> {

    public static class MovieTrailerViewHolder extends RecyclerView.ViewHolder {
        private ImageView previewImageView;
        private TextView labelTextView;

        public MovieTrailerViewHolder(View itemView) {
            super(itemView);
            previewImageView =
                    (ImageView) itemView.findViewById(R.id.movie_trailer_preview_imageview);
            labelTextView = (TextView) itemView.findViewById(R.id.movie_trailer_preview_label);
        }
    }

    public MovieTrailerAdapter(final Context context) {
        super(context);
    }

    @Override
    protected RecyclerView.ViewHolder createNormalViewHolder(ViewGroup parent) {
        final View listItem = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movie_trailer_item, parent, false);
        return new MovieTrailerViewHolder(listItem);
    }

    @Override
    protected int getNoDataViewLayoutId() {
        return R.layout.trailers_not_available_textview;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(getItem(position) == null) {
            return;
        }
        final MovieTrailerViewHolder movieTrailerViewHolder = (MovieTrailerViewHolder) holder;
        final MovieTrailer movieTrailer = getItem(position);
        Glide.with(movieTrailerViewHolder.previewImageView.getContext())
                .load(MovieDbUtil.buildYouTubeThumbnailUrl(getContext(), movieTrailer.getKey()))
                .centerCrop()
                .placeholder(R.drawable.image_placeholder_horizontal)
                .error(R.drawable.image_na)
                .into(movieTrailerViewHolder.previewImageView);
        setupImageViewToOpenYoutube(movieTrailerViewHolder.previewImageView, movieTrailer);

        movieTrailerViewHolder.labelTextView.setText(
                getContext().getString(R.string.moviedb_trailer_thumbnail_prefix) + (position + 1));
    }

    private void setupImageViewToOpenYoutube(final ImageView imageView,
                                             final MovieTrailer movieTrailer) {
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(movieTrailer.getUrl()));
                getContext().startActivity(intent);
            }
        });
    }
}
