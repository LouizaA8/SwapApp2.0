package com.example.swapapp20;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostDetailPagerAdapter extends RecyclerView.Adapter<PostDetailPagerAdapter.PostViewHolder> {
    private static final String TAG = "PostDetailPagerAdapter";
    private final Context context;
    private final List<Post> posts;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;

    public PostDetailPagerAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
        this.firestore = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        Log.d(TAG, "Adapter created with " + posts.size() + " posts");
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post_detail, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        Log.d(TAG, "Binding post at position " + position + " with ID: " + post.getPostId());

        // Load the post image
        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(post.getImageUrl())
                    .centerInside()
                    .placeholder(R.drawable.profile)
                    .error(R.drawable.profile)
                    .into(holder.postImage);
        } else {
            holder.postImage.setImageResource(R.drawable.profile);
        }

        // Set the post description
        holder.postDescription.setText(post.getCaption() != null ? post.getCaption() : "");

        // Load user information
        loadUserInfo(post.getUserId(), holder);

        // Setup swap button and check if the current user has already swapped this post
        setupSwapButton(holder, post);
    }

    private void loadUserInfo(String userId, PostViewHolder holder) {
        firestore.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("name");
                        String profileImageUrl = documentSnapshot.getString("coverPhotoUrl");

                        holder.postUsername.setText(username != null ? username : "Unknown User");

                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Glide.with(context)
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.person)
                                    .error(R.drawable.person)
                                    .into(holder.profileImage);
                        } else {
                            holder.profileImage.setImageResource(R.drawable.person);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading user info", e));
    }

    private void setupSwapButton(PostViewHolder holder, Post post) {
        String currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        // Disable swap button if post belongs to current user or user is not logged in
        if (currentUserId == null || currentUserId.equals(post.getUserId())) {
            holder.swapButton.setEnabled(false);
            holder.swapButton.setText(currentUserId != null ? "Your Post" : "Login to Swap");
            return;
        }

        DocumentReference postRef = firestore.collection("posts").document(post.getPostId());

        // Check if user has already swapped this post
        checkSwapStatus(holder, currentUserId, postRef);

        // Setup click listener for the swap button
        setupSwapButtonClickListener(holder, post, currentUserId, postRef);
    }

    private void checkSwapStatus(PostViewHolder holder, String currentUserId, DocumentReference postRef) {
        postRef.collection("swaps")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    holder.swapButton.setText(documentSnapshot.exists() ? "Unswap" : "Swap");
                    holder.swapButton.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking swap status", e);
                    holder.swapButton.setEnabled(true);
                    holder.swapButton.setText("Swap");
                });
    }

    private void setupSwapButtonClickListener(PostViewHolder holder, Post post,
                                              String currentUserId, DocumentReference postRef) {
        holder.swapButton.setOnClickListener(v -> {
            holder.swapButton.setEnabled(false);
            performSwapTransaction(holder, post, currentUserId, postRef);
        });
    }

    private void performSwapTransaction(PostViewHolder holder, Post post,
                                        String currentUserId, DocumentReference postRef) {
        firestore.runTransaction(transaction -> {
            DocumentSnapshot postSnapshot = transaction.get(postRef);
            DocumentSnapshot swapSnapshot = transaction.get(
                    postRef.collection("swaps").document(currentUserId)
            );

            boolean isSwapped = swapSnapshot.exists();
            long currentSwaps = postSnapshot.getLong("swaps") != null ?
                    postSnapshot.getLong("swaps") : 0;

            if (isSwapped) {
                transaction.delete(postRef.collection("swaps").document(currentUserId));
                transaction.update(postRef, "swaps", Math.max(0, currentSwaps - 1));
                return false;
            } else {
                Map<String, Object> swapData = new HashMap<>();
                swapData.put("userId", currentUserId);
                swapData.put("timestamp", System.currentTimeMillis());

                transaction.set(postRef.collection("swaps").document(currentUserId), swapData);
                transaction.update(postRef, "swaps", currentSwaps + 1);
                return true;
            }
        }).addOnSuccessListener(isSwapped -> {
            Log.d(TAG, "Swap transaction successful: " + (isSwapped ? "Swapped" : "Unswapped"));
            handleSwapSuccess(holder, post, currentUserId, postRef, isSwapped);
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error in swap transaction", e);
            handleSwapFailure(holder, e);
        });
    }

    private void handleSwapSuccess(PostViewHolder holder, Post post, String currentUserId,
                                   DocumentReference postRef, boolean isSwapped) {
        if (isSwapped) {
            // Create the swap notification
            createSwapNotification(post, currentUserId);

            // Check for matches when a new swap is created
            MatchDetectionSystem matchSystem = new MatchDetectionSystem();
            matchSystem.checkForMatch(currentUserId, post.getUserId(), post.getPostId());
        }

        updateUIAfterSwap(holder, isSwapped);
    }

    private void createSwapNotification(Post post, String currentUserId) {
        // First get the current user's document to access their name
        firestore.collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    // Get the name field from the swapper's user document
                    String swapperName;

                    if (userDoc.exists()) {
                        swapperName = userDoc.getString("name");
                        Log.d(TAG, "Found swapper's name: " + swapperName + " for user ID: " + currentUserId);
                    } else {
                        swapperName = null;
                        Log.e(TAG, "Swapper's user document doesn't exist for ID: " + currentUserId);
                    }

                    // Create and save notification with this name
                    Map<String, Object> notificationData = new HashMap<>();
                    notificationData.put("imageUrl", post.getImageUrl());
                    notificationData.put("message", "requested a swap");
                    notificationData.put("postId", post.getPostId());
                    notificationData.put("receiverId", post.getUserId());
                    notificationData.put("senderId", currentUserId);

                    // Use the actual name we found, or "Unknown user" if null
                    notificationData.put("senderUsername", swapperName != null ? swapperName : "Unknown user");
                    notificationData.put("timestamp", System.currentTimeMillis());

                    // Save notification to Firestore
                    firestore.collection("notifications")
                            .add(notificationData)
                            .addOnSuccessListener(docRef -> {
                                // Update the notification with its own ID
                                String notificationId = docRef.getId();
                                firestore.collection("notifications")
                                        .document(notificationId)
                                        .update("id", notificationId);

                                Log.d(TAG, "Successfully created notification with name: " +
                                        (swapperName != null ? swapperName : "Unknown user"));
                            })
                            .addOnFailureListener(e ->
                                    Log.e(TAG, "Failed to create notification", e));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get swapper's user document", e);

                    // Create fallback notification if we can't get the user document
                    Map<String, Object> fallbackNotification = new HashMap<>();
                    fallbackNotification.put("imageUrl", post.getImageUrl());
                    fallbackNotification.put("message", "requested to swap with your item");
                    fallbackNotification.put("postId", post.getPostId());
                    fallbackNotification.put("receiverId", post.getUserId());
                    fallbackNotification.put("senderId", currentUserId);
                    fallbackNotification.put("senderUsername", "A user");
                    fallbackNotification.put("timestamp", System.currentTimeMillis());

                    firestore.collection("notifications").add(fallbackNotification);
                });
    }

    private void updateUIAfterSwap(PostViewHolder holder, boolean isSwapped) {
        holder.swapButton.setText(isSwapped ? "Unswap" : "Swap");
        holder.swapButton.setEnabled(true);
        Toast.makeText(context, isSwapped ? "Swap request sent" : "Swap request undone",
                Toast.LENGTH_SHORT).show();
    }

    private void handleSwapFailure(PostViewHolder holder, Exception e) {
        Log.e(TAG, "Error updating swap status", e);
        holder.swapButton.setEnabled(true);
        Toast.makeText(context, "Error updating swap status: " + e.getMessage(),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView postImage;
        ImageView profileImage;
        TextView postUsername;
        TextView postDescription;
        Button swapButton;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            postImage = itemView.findViewById(R.id.detailPostImage);
            profileImage = itemView.findViewById(R.id.detailProfileImage);
            postUsername = itemView.findViewById(R.id.detailPostUsername);
            postDescription = itemView.findViewById(R.id.detailPostDescription);
            swapButton = itemView.findViewById(R.id.detailSwapButton);
        }
    }
}