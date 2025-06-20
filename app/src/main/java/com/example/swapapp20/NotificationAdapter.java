package com.example.swapapp20;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private static final String TAG = "NotificationAdapter";
    private final Context context;
    private final List<NotificationModel> notifications;
    private final FirebaseFirestore firestore;

    public NotificationAdapter(Context context, List<NotificationModel> notifications) {
        this.context = context;
        this.notifications = notifications;
        this.firestore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationModel notification = notifications.get(position);

        // Load notification post image
        if (notification.getImageUrl() != null && !notification.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(notification.getImageUrl())
                    .placeholder(R.drawable.profile)
                    .error(R.drawable.profile)
                    .into(holder.notificationImage);
        }

        // Load sender's profile image
        loadSenderProfileImage(holder.profileImage, notification.getSenderId());

        String message = String.format("%s %s", notification.getSenderUsername(), notification.getMessage());
        holder.notificationMessage.setText(message);

        // Set time ago
        holder.timeAgo.setText(getTimeAgo(notification.getTimestamp()));

        // Set click listener to navigate to user profile for all notification types
        holder.itemView.setOnClickListener(v -> {
            if (notification.getSenderId() != null && !notification.getSenderId().isEmpty()) {
                navigateToUserProfile(notification.getSenderId());
            }
        });
    }

    private void loadSenderProfileImage(ImageView profileImage, String senderId) {
        if (senderId == null || senderId.isEmpty()) {
            profileImage.setImageResource(R.drawable.profile);
            return;
        }

        firestore.collection("users").document(senderId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String profileImageUrl = documentSnapshot.getString("coverPhotoUrl");
                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Glide.with(context.getApplicationContext())
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.profile)
                                    .error(R.drawable.profile)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .into(profileImage);
                        } else {
                            profileImage.setImageResource(R.drawable.profile);
                        }
                    } else {
                        profileImage.setImageResource(R.drawable.profile);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading sender profile image", e);
                    profileImage.setImageResource(R.drawable.profile);
                });
    }

    private void navigateToUserProfile(String userId) {
        ProfileFragment profileFragment = ProfileFragment.newInstance(userId);
        FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();

        fragmentManager.beginTransaction()
                .replace(R.id.frame_container, profileFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView notificationImage;
        TextView notificationMessage;
        TextView timeAgo;
        ImageView profileImage;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            notificationImage = itemView.findViewById(R.id.postImage);
            notificationMessage = itemView.findViewById(R.id.notificationMessage);
            timeAgo = itemView.findViewById(R.id.timeAgo);
            profileImage = itemView.findViewById(R.id.profileImage);
        }
    }

    private String getTimeAgo(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        // Convert to seconds
        long seconds = diff / 1000;
        if (seconds < 60) return "just now";

        // Convert to minutes
        long minutes = seconds / 60;
        if (minutes < 60) return minutes + " minutes ago";

        // Convert to hours
        long hours = minutes / 60;
        if (hours < 24) return hours + " hours ago";

        // Convert to days
        long days = hours / 24;
        if (days < 7) return days + " days ago";

        // Convert to weeks
        long weeks = days / 7;
        if (weeks < 4) return weeks + " weeks ago";

        // Convert to months
        long months = days / 30;
        return months + " months ago";
    }
}