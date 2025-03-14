package com.example.swapapp20;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
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
import de.hdodenhof.circleimageview.CircleImageView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private static final String TAG = "PostAdapter";
    private final Context context;
    private final List<Post> postList;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;

    public PostAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
        this.firestore = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.post = post;
        if (!isValidBinding(post, holder)) return;

        loadPostImage(holder.postImage, post.getImageUrl());
        loadUserProfileData(holder, post.getUserId());

        setupSwapButton(holder, post);
    }

    private boolean isValidBinding(Post post, PostViewHolder holder) {
        return post != null && context != null && holder != null &&
                auth.getCurrentUser() != null;
    }

    private void loadUserProfileData(PostViewHolder holder, String userId) {
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "Invalid userId for loadUserProfileData");
            holder.userProfileImage.setImageResource(R.drawable.profile);
            holder.userLocation.setText("Unknown location"); // Default if no location is found
            holder.userName.setText("Unknown user");

            // Set caption without username since we don't have one
            holder.caption.setText(holder.post.getCaption() != null ? holder.post.getCaption() : "");
            return;
        }

        firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Load profile image
                        String profileImageUrl = documentSnapshot.getString("coverPhotoUrl");
                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Glide.with(context.getApplicationContext())
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.profile)
                                    .error(R.drawable.profile)
                                    .into(holder.userProfileImage);
                        } else {
                            holder.userProfileImage.setImageResource(R.drawable.profile);
                        }

                        // Load username
                        String username = documentSnapshot.getString("name");
                        if (username != null && !username.isEmpty()) {
                            holder.userName.setText(username);

                            // Now set the caption with the username
                            setCaptionWithUsername(holder, holder.post.getCaption(), username);
                        } else {
                            holder.userName.setText("No username");
                            holder.caption.setText(holder.post.getCaption() != null ? holder.post.getCaption() : "");
                        }

                        // Load location
                        String location = documentSnapshot.getString("location");
                        if (location != null && !location.isEmpty()) {
                            holder.userLocation.setText(location);
                        } else {
                            holder.userLocation.setText("No location");
                        }
                    } else {
                        holder.userProfileImage.setImageResource(R.drawable.profile);
                        holder.userName.setText("Unknown user");
                        holder.userLocation.setText("Unknown location");
                        holder.caption.setText(holder.post.getCaption() != null ? holder.post.getCaption() : "");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading user data", e);
                    holder.userProfileImage.setImageResource(R.drawable.profile);
                    holder.userName.setText("Unknown user");
                    holder.userLocation.setText("Unknown location");
                    holder.caption.setText(holder.post.getCaption() != null ? holder.post.getCaption() : "");
                });
    }

    // New helper method to set the caption with username in bold
    private void setCaptionWithUsername(PostViewHolder holder, String caption, String username) {
        caption = caption != null ? caption : "";

        // Create a SpannableString to make the username bold
        SpannableString spannableString = new SpannableString(username + " " + caption);
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, username.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Set the formatted text to the caption TextView
        holder.caption.setText(spannableString);
    }
    private void loadPostImage(ImageView imageView, String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .error(R.drawable.profile)
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.profile);
        }
    }



    private void setupSwapButton(PostViewHolder holder, Post post) {
        String currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (currentUserId == null) {
            Log.e(TAG, "Current user ID is null");
            return;
        }

        DocumentReference postRef = firestore.collection("posts").document(post.getPostId());
        if (postRef == null) {
            Log.e(TAG, "Post reference is null");
            return;
        }

        checkSwapStatus(holder, currentUserId, postRef);
        setupSwapButtonClickListener(holder, post, currentUserId, postRef);
    }

    private void checkSwapStatus(PostViewHolder holder, String currentUserId,
                                 DocumentReference postRef) {
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
                transaction.update(postRef, "swaps", currentSwaps - 1);
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
            // Create the regular swap notification
            createSwapNotification(post, currentUserId);

            // Check for matches when a new swap is created
            MatchDetectionSystem matchSystem = new MatchDetectionSystem();
            matchSystem.checkForMatch(currentUserId, post.getUserId(), post.getPostId());
        }

        updateUIAfterSwap(holder, isSwapped);
    }


    private void createSwapNotification(Post post, String currentUserId) {
        // Step 1: First get the current user's document to access their name
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

                    // Step 2: Create and save notification with this name
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

    // Add a fallback method if we can't get the user's name


    private void updateUIAfterSwap(PostViewHolder holder,  boolean isSwapped) {

        holder.swapButton.setText(isSwapped ? "Unswap" : "Swap");
        holder.swapButton.setEnabled(true);
        Toast.makeText(context, isSwapped ? "Swap request sent" : "swap request undone",
                Toast.LENGTH_SHORT).show();
    }

    private void handleSwapFailure(PostViewHolder holder, Exception e) {
        Log.e(TAG, "Error updating swap status", e);
        holder.swapButton.setEnabled(true);
        Toast.makeText(context, "Error updating swap status: " + e.getMessage(),
                Toast.LENGTH_SHORT).show();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return postList != null ? postList.size() : 0;
    }


    public static class PostViewHolder extends RecyclerView.ViewHolder {
        final ImageView postImage;
        final TextView caption, userLocation, userName;
        final Button swapButton;
        final CircleImageView userProfileImage;
        Post post;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            postImage = itemView.findViewById(R.id.postImage);
            caption = itemView.findViewById(R.id.caption);
            swapButton = itemView.findViewById(R.id.swapButton);
            userProfileImage = itemView.findViewById(R.id.userProfileImage);
            userLocation = itemView.findViewById(R.id.userLocation);
            userName = itemView.findViewById(R.id.userName);
        }

    }

}