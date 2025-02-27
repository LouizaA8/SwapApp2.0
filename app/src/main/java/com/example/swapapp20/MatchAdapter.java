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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MatchAdapter extends RecyclerView.Adapter<MatchAdapter.MatchViewHolder> {
    private static final String TAG = "MatchAdapter";

    private final Context context;
    private final List<Match> matches;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;

    public MatchAdapter(Context context, List<Match> matches) {
        this.context = context;
        this.matches = matches;
        this.firestore = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public MatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.match_list_item, parent, false);
        return new MatchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchViewHolder holder, int position) {
        Match match = matches.get(position);
        String currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";

        // Get the other user's ID
        String otherUserId = match.getOtherUserId(currentUserId);

        // Load the other user's details
        loadUserDetails(holder, otherUserId);

        // Format and display match date
        if (match.getTimestamp() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
            String formattedDate = dateFormat.format(match.getTimestamp());
            holder.matchDate.setText(formattedDate);
        } else {
            holder.matchDate.setText("Recent match");
        }

        // Set click listener to navigate to chat or user profile
        holder.itemView.setOnClickListener(v -> {
            // You can implement navigation to a chat screen or user profile here
            navigateToUserProfile(otherUserId);
        });
    }

    private void loadUserDetails(MatchViewHolder holder, String userId) {
        firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Load username
                        String username = documentSnapshot.getString("name");
                        if (username != null && !username.isEmpty()) {
                            holder.userName.setText(username);
                        } else {
                            holder.userName.setText("Unknown User");
                        }

                        // Load user location
                        String location = documentSnapshot.getString("location");
                        if (location != null && !location.isEmpty()) {
                            holder.userLocation.setText(location);
                        } else {
                            holder.userLocation.setText("Unknown location");
                        }

                        // Load profile image
                        String profileImageUrl = documentSnapshot.getString("coverPhotoUrl");
                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Glide.with(context.getApplicationContext())
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.profile)
                                    .error(R.drawable.profile)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .into(holder.userImage);
                        } else {
                            holder.userImage.setImageResource(R.drawable.profile);
                        }
                    } else {
                        holder.userName.setText("Unknown User");
                        holder.userLocation.setText("Unknown location");
                        holder.userImage.setImageResource(R.drawable.profile);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading user details", e);
                    holder.userName.setText("Unknown User");
                    holder.userLocation.setText("Unknown location");
                    holder.userImage.setImageResource(R.drawable.profile);
                });
    }

    private void navigateToUserProfile(String userId) {
        ProfileFragment profileFragment = ProfileFragment.newInstance(userId);
        ((FragmentActivity) context).getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_container, profileFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public int getItemCount() {
        return matches != null ? matches.size() : 0;
    }

    static class MatchViewHolder extends RecyclerView.ViewHolder {
        final ImageView userImage;
        final TextView userName;
        final TextView userLocation;
        final TextView matchDate;

        MatchViewHolder(@NonNull View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.userImage);
            userName = itemView.findViewById(R.id.userName);
            userLocation = itemView.findViewById(R.id.userLocation);
            matchDate = itemView.findViewById(R.id.matchDate);
        }
    }
}