/*package com.example.swapapp20;

import android.app.Notification;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.swapapp20.R;

import java.util.List;

public class LikesAdapter extends RecyclerView.Adapter<LikesAdapter.LikesViewHolder> {
    private final List<Notification> notificationsList;

    public LikesAdapter(List<Notification> notificationsList) {
        this.notificationsList = notificationsList;
    }

    @NonNull
    @Override
    public LikesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new LikesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LikesViewHolder holder, int position) {
        if (position >= notificationsList.size()) return;

        Notification notification = notificationsList.get(position);
        if (notification == null) return;

        holder.message.setText(notification.getMessage());

        // Load post image if available
        if (notification.getImageUrl() != null && !notification.getImageUrl().isEmpty()) {
            holder.postImage.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(notification.getImageUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.profile) // Make sure to add a placeholder image
                    .error(R.drawable.profile) // Make sure to add an error image
                    .into(holder.postImage);
        } else {
            holder.postImage.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return notificationsList != null ? notificationsList.size() : 0;
    }

    public static class LikesViewHolder extends RecyclerView.ViewHolder {
        TextView message;
        ImageView postImage;

        public LikesViewHolder(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.notificationMessage);
            postImage = itemView.findViewById(R.id.notificationPostImage);
        }
    }
}*/