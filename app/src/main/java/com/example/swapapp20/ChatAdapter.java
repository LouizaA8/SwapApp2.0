package com.example.swapapp20;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private static final String TAG = "ChatAdapter";

    private final Context context;
    private final List<ChatModel> chats;
    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;

    public ChatAdapter(Context context, List<ChatModel> chats) {
        this.context = context;
        this.chats = chats;
        this.auth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_list_item, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatModel chat = chats.get(position);
        String currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";

        // Get the other user ID
        String otherUserId = chat.getOtherUserId(currentUserId);

        // Load other user details
        loadUserDetails(holder, otherUserId);

        // Set last message
        if (chat.getLastMessage() != null) {
            holder.lastMessage.setText(chat.getLastMessage());
        } else {
            holder.lastMessage.setText("Start a conversation");
        }

        // Format and display the timestamp
        if (chat.getLastMessageTimestamp() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
            String formattedTime = dateFormat.format(chat.getLastMessageTimestamp());
            holder.timestamp.setText(formattedTime);
        } else {
            holder.timestamp.setText("");
        }

        // Show unread message count
        if (chat.getUnreadCount() != null) {
            Map<String, Integer> unreadCount = chat.getUnreadCount();
            Integer count = unreadCount.get(currentUserId);

            if (count != null && count > 0) {
                holder.unreadCount.setVisibility(View.VISIBLE);
                holder.unreadCount.setText(String.valueOf(count));
            } else {
                holder.unreadCount.setVisibility(View.GONE);
            }
        } else {
            holder.unreadCount.setVisibility(View.GONE);
        }

        // Set click listener to open chat conversation
        holder.itemView.setOnClickListener(v -> {
            ChatsFragment.markChatAsRead(chat.getId(), currentUserId);

            // Start a chat conversation activity
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("chatId", chat.getId());
            intent.putExtra("otherUserId", otherUserId);
            context.startActivity(intent);
        });
    }

    private void loadUserDetails(ChatViewHolder holder, String userId) {
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
                        holder.userImage.setImageResource(R.drawable.profile);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading user details", e);
                    holder.userName.setText("Unknown User");
                    holder.userImage.setImageResource(R.drawable.profile);
                });
    }

    @Override
    public int getItemCount() {
        return chats != null ? chats.size() : 0;
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        final CircleImageView userImage;
        final TextView userName;
        final TextView lastMessage;
        final TextView timestamp;
        final TextView unreadCount;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.userImageChats);
            userName = itemView.findViewById(R.id.userNameChats);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            timestamp = itemView.findViewById(R.id.timestampChats);
            unreadCount = itemView.findViewById(R.id.unreadCount);
        }
    }
}