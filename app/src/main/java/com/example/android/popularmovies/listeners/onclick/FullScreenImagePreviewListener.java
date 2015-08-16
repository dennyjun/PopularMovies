package com.example.android.popularmovies.listeners.onclick;

import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.android.popularmovies.R;

/**
 * Created by Denny on 8/16/2015.
 */
public class FullScreenImagePreviewListener implements View.OnClickListener {
    final String imageUrl;

    public FullScreenImagePreviewListener(final String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public void onClick(View v) {
        final Dialog dialog = createDialog(v);
        dialog.show();
    }

    private Dialog createDialog(final View v) {
        final Dialog dialog = new Dialog(v.getContext(),
                android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(createImageView(v, dialog));
        return dialog;
    }

    private ImageView createImageView(final View v, final Dialog dialog) {
        final ImageView fullScreenImageView = (ImageView) LayoutInflater.from(v.getContext())
                .inflate(R.layout.full_screen_image, (ViewGroup) v.getParent(), false);
        fullScreenImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Glide.with(v.getContext())
                .load(imageUrl)
                .fitCenter()
                .placeholder(R.drawable.image_placeholder)
                .error(R.drawable.image_na)
                .into(fullScreenImageView);

        return fullScreenImageView;
    }
}
