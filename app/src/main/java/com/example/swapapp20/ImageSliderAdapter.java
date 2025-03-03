package com.example.swapapp20;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.SliderViewHolder> {
    private static final String TAG = "ImageSliderAdapter";
    private List<SlideItem> slideItems;

    public ImageSliderAdapter(List<SlideItem> slideItems) {
        this.slideItems = slideItems;
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        try {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.slider_item, parent, false);
            return new SliderViewHolder(view);
        } catch (Exception e) {
            Log.e(TAG, "Error creating ViewHolder: " + e.getMessage());
            View fallbackView = new View(parent.getContext());
            fallbackView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            return new SliderViewHolder(fallbackView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        try {
            if (position >= 0 && position < slideItems.size()) {
                SlideItem slideItem = slideItems.get(position);
                if (slideItem != null) {
                    Glide.with(holder.imageView.getContext())
                            .load(slideItem.getImageResource())
                            .into(holder.imageView);
                } else {
                    Log.e(TAG, "Null slide item at position: " + position);
                }
            } else {
                Log.e(TAG, "Invalid position: " + position);
            }
        } catch (OutOfMemoryError oom) {
            Log.e(TAG, "Out of memory error loading image at position: " + position, oom);
        } catch (Exception e) {
            Log.e(TAG, "Error binding ViewHolder at position " + position + ": " + e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return slideItems != null ? slideItems.size() : 0;
    }

    static class SliderViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        SliderViewHolder(@NonNull View itemView) {
            super(itemView);
            try {
                imageView = itemView.findViewById(R.id.imageView);
            } catch (Exception e) {
                Log.e("SliderViewHolder", "Error finding imageView: " + e.getMessage());
            }
        }
    }
}