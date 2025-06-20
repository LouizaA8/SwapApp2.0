package com.example.swapapp20;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
            navigateToChat(otherUserId);
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

    private void navigateToChat(String userId) {
        String currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
        final boolean[] chatFound = {false};  // Use array to modify in lambda

        firestore.collection("chats")
                .whereArrayContains("participants", currentUserId)
                .get()
                .addOnSuccessListener(queryDocuments -> {
                    for (DocumentSnapshot doc : queryDocuments) {
                        List<String> participants = (List<String>) doc.get("participants");
                        if (participants != null && participants.contains(userId)) {
                            chatFound[0] = true;
                            String chatId = doc.getId();

                            // Navigate to the chat detail fragment
                            Intent intent = new Intent(context, ChatActivity.class);
                            intent.putExtra("chatId", chatId);
                            intent.putExtra("otherUserId", userId);
                            context.startActivity(intent);
                            break;  // Exit loop once we find a chat
                        }
                    }

                    // If no chat was found, create a new one
                    if (!chatFound[0]) {
                        createNewChat(currentUserId, userId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error finding chat", e);
                    Toast.makeText(context, "Error finding chat", Toast.LENGTH_SHORT).show();
                });
    }

    private void createNewChat(String currentUserId, String otherUserId) {
        // Create a list of participants
        List<String> participants = java.util.Arrays.asList(currentUserId, otherUserId);

        // Create a new chat document
        ChatModel newChat = new ChatModel();
        newChat.setParticipants(participants);
        // Set initial unread counts to 0
        Map<String, Integer> unreadCount = new HashMap<>();
        unreadCount.put(currentUserId, 0);
        unreadCount.put(otherUserId, 0);
        newChat.setUnreadCount(unreadCount);
        // Set creation timestamp
        newChat.setLastMessageTimestamp(new Date());

        // Add to Firestore
        firestore.collection("chats")
                .add(newChat)
                .addOnSuccessListener(documentReference -> {
                    String chatId = documentReference.getId();

                    // Navigate to the new chat
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("chatId", chatId);
                    intent.putExtra("otherUserId", otherUserId);
                    context.startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating new chat", e);
                    Toast.makeText(context, "Error creating chat", Toast.LENGTH_SHORT).show();
                });
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