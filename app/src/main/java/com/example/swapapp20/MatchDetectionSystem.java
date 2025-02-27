package com.example.swapapp20;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatchDetectionSystem {
    private static final String TAG = "MatchDetectionSystem";
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;

    public MatchDetectionSystem() {
        this.firestore = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    /**
     * Check if two users have a match based on mutual swap requests
     * This should be called whenever a user sends a swap request
     *
     * @param currentUserId The ID of the current user
     * @param otherUserId The ID of the post owner
     * @param postId The ID of the post that was swapped
     */
    public void checkForMatch(String currentUserId, String otherUserId, String postId) {
        if (currentUserId == null || otherUserId == null || postId == null) {
            Log.e(TAG, "Invalid parameters for checkForMatch");
            return;
        }

        // Don't check for matches with yourself
        if (currentUserId.equals(otherUserId)) {
            return;
        }

        Log.d(TAG, "Checking for match between " + currentUserId + " and " + otherUserId);

        // Find posts by the current user
        firestore.collection("posts")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(userPosts -> {
                    if (userPosts.isEmpty()) {
                        Log.d(TAG, "Current user has no posts");
                        return;
                    }

                    // For each post by the current user
                    for (QueryDocumentSnapshot userPost : userPosts) {
                        String userPostId = userPost.getId();

                        // Check if the other user has swapped this post
                        firestore.collection("posts")
                                .document(userPostId)
                                .collection("swaps")
                                .document(otherUserId)
                                .get()
                                .addOnSuccessListener(swapDoc -> {
                                    if (swapDoc.exists()) {
                                        // Found a matching swap!
                                        Log.d(TAG, "Match found! Both users have swapped each other's posts");
                                        // Create match record in Firestore
                                        createMatchRecord(currentUserId, otherUserId, postId, userPostId);
                                        // Send match notifications to both users
                                        sendMatchNotifications(currentUserId, otherUserId, postId, userPostId);
                                    } else {
                                        Log.d(TAG, "No mutual swap found for this post");
                                    }
                                })
                                .addOnFailureListener(e -> Log.e(TAG, "Error checking for swap", e));
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error finding user posts", e));
    }

    /**
     * Create a record of the match in the database
     */
    private void createMatchRecord(String user1Id, String user2Id, String post1Id, String post2Id) {
        // Create a unique match ID (can be customized as needed)
        String matchId = user1Id + "_" + user2Id + "_" + System.currentTimeMillis();

        Map<String, Object> matchData = new HashMap<>();
        matchData.put("user1Id", user1Id);
        matchData.put("user2Id", user2Id);
        matchData.put("post1Id", post1Id);
        matchData.put("post2Id", post2Id);
        matchData.put("timestamp", FieldValue.serverTimestamp());
        matchData.put("isActive", true);

        // Add users array for querying
        List<String> users = new ArrayList<>();
        users.add(user1Id);
        users.add(user2Id);
        matchData.put("users", users);

        firestore.collection("matches")
                .document(matchId)
                .set(matchData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Match record created successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Error creating match record", e));
    }

    /**
     * Send match notifications to both users involved in the match
     */
    private void sendMatchNotifications(String user1Id, String user2Id, String post1Id, String post2Id) {
        // Get the post images to include in the notifications
        getPostDetails(post1Id, post1Image -> {
            getPostDetails(post2Id, post2Image -> {
                getUserDetails(user1Id, user1Name -> {
                    getUserDetails(user2Id, user2Name -> {
                        // Send notification to user 1
                        createMatchNotification(user1Id, user2Id, user2Name, post2Image);

                        // Send notification to user 2
                        createMatchNotification(user2Id, user1Id, user1Name, post1Image);
                    });
                });
            });
        });
    }

    /**
     * Create a match notification for a specific user
     */
    private void createMatchNotification(String receiverId, String senderId, String senderName, String imageUrl) {
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("imageUrl", imageUrl);
        notificationData.put("message", "matched with you! You can now chat with each other.");
        notificationData.put("receiverId", receiverId);
        notificationData.put("senderId", senderId);
        notificationData.put("senderUsername", senderName != null ? senderName : "A user");
        notificationData.put("timestamp", System.currentTimeMillis());
        notificationData.put("type", "match"); // Adding a type field to differentiate match notifications

        firestore.collection("notifications")
                .add(notificationData)
                .addOnSuccessListener(docRef -> {
                    // Update the notification with its own ID
                    String notificationId = docRef.getId();
                    firestore.collection("notifications")
                            .document(notificationId)
                            .update("id", notificationId);

                    Log.d(TAG, "Match notification created successfully");
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error creating match notification", e));
    }

    /**
     * Get a user's name from their profile
     */
    private void getUserDetails(String userId, UserDetailCallback callback) {
        firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        callback.onUserDetailsFetched(name);
                    } else {
                        callback.onUserDetailsFetched(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user details", e);
                    callback.onUserDetailsFetched(null);
                });
    }

    /**
     * Get a post's image URL
     */
    private void getPostDetails(String postId, PostDetailCallback callback) {
        firestore.collection("posts")
                .document(postId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String imageUrl = documentSnapshot.getString("imageUrl");
                        callback.onPostDetailsFetched(imageUrl);
                    } else {
                        callback.onPostDetailsFetched(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching post details", e);
                    callback.onPostDetailsFetched(null);
                });
    }

    // Callback interfaces
    interface UserDetailCallback {
        void onUserDetailsFetched(String name);
    }

    interface PostDetailCallback {
        void onPostDetailsFetched(String imageUrl);
    }
}