package com.example.swapapp20;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";

    private String chatId;
    private String otherUserId;
    private String currentUserId;

    private RecyclerView recyclerViewMessages;
    private MessageAdapter messageAdapter;
    private List<MessageModel> messagesList;

    private EditText messageInput;
    private ImageButton sendButton;
    private Toolbar toolbar;
    private ImageView userImage;
    private TextView userName;

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        try {
            // Get data from intent
            chatId = getIntent().getStringExtra("chatId");
            otherUserId = getIntent().getStringExtra("otherUserId");

            // If otherUserId is null, try the alternative key
            if (otherUserId == null) {
                otherUserId = getIntent().getStringExtra("userId");
            }

            if (chatId == null || otherUserId == null) {
                Toast.makeText(this, "Error loading chat", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Intent extras - chatId: " + chatId + ", otherUserId: " + otherUserId
                        + ", userId: " + getIntent().getStringExtra("userId"));
                finish();
                return;
            }

            // Initialize Firebase
            firestore = FirebaseFirestore.getInstance();
            auth = FirebaseAuth.getInstance();

            if (auth.getCurrentUser() == null) {
                Toast.makeText(this, "Authentication error", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            currentUserId = auth.getCurrentUser().getUid();

            // Initialize UI components
            toolbar = findViewById(R.id.chatToolbar);
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }

            userImage = findViewById(R.id.userImage);
            userName = findViewById(R.id.userName);

            recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
            messageInput = findViewById(R.id.messageInput);
            sendButton = findViewById(R.id.sendButton);

            // Setup RecyclerView
            messagesList = new ArrayList<>();
            messageAdapter = new MessageAdapter(this, messagesList, currentUserId);

            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setStackFromEnd(true);
            recyclerViewMessages.setLayoutManager(layoutManager);
            recyclerViewMessages.setAdapter(messageAdapter);

            // Load user details
            loadUserDetails();

            // Load messages
            loadMessages();

            // Mark chat as read
            try {
                ChatsFragment.markChatAsRead(chatId, currentUserId);
            } catch (Exception e) {
                Log.e(TAG, "Error marking chat as read", e);
            }

            // Set send button click listener
            sendButton.setOnClickListener(v -> sendMessage());

            // Back button listener
            toolbar.setNavigationOnClickListener(v -> onBackPressed());

        } catch (Exception e) {
            Log.e(TAG, "Error during onCreate", e);
            Toast.makeText(this, "Error initializing chat", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadUserDetails() {
        try {
            firestore.collection("users").document(otherUserId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Set username
                            String name = documentSnapshot.getString("name");
                            if (name != null && !name.isEmpty()) {
                                userName.setText(name);
                            } else {
                                userName.setText("User");
                            }

                            // Set profile image
                            String profileImageUrl = documentSnapshot.getString("coverPhotoUrl");
                            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                Glide.with(getApplicationContext())
                                        .load(profileImageUrl)
                                        .placeholder(R.drawable.profile)
                                        .error(R.drawable.profile)
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .into(userImage);
                            } else {
                                userImage.setImageResource(R.drawable.profile);
                            }
                        } else {
                            userName.setText("User");
                            userImage.setImageResource(R.drawable.profile);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading user details", e);
                        userName.setText("User");
                        userImage.setImageResource(R.drawable.profile);
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in loadUserDetails", e);
            userName.setText("User");
            userImage.setImageResource(R.drawable.profile);
        }
    }

    private void loadMessages() {
        try {
            firestore.collection("chats").document(chatId)
                    .collection("messages")
                    .orderBy("timestamp", Query.Direction.ASCENDING)
                    .addSnapshotListener((value, error) -> {
                        if (error != null) {
                            Log.e(TAG, "Error loading messages", error);
                            Toast.makeText(ChatActivity.this, "Error loading messages", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (value != null) {
                            messagesList.clear();

                            for (DocumentSnapshot doc : value.getDocuments()) {
                                try {
                                    MessageModel message = doc.toObject(MessageModel.class);
                                    if (message != null) {
                                        message.setId(doc.getId());
                                        messagesList.add(message);
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error processing message document", e);
                                }
                            }

                            messageAdapter.notifyDataSetChanged();

                            // Scroll to the bottom
                            if (messagesList.size() > 0) {
                                recyclerViewMessages.smoothScrollToPosition(messagesList.size() - 1);
                            }

                            // Mark messages as read
                            markMessagesAsRead();
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in loadMessages", e);
            Toast.makeText(this, "Error loading messages", Toast.LENGTH_SHORT).show();
        }
    }

    private void markMessagesAsRead() {
        try {
            firestore.collection("chats").document(chatId)
                    .collection("messages")
                    .whereEqualTo("read", false)
                    .whereEqualTo("senderId", otherUserId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            doc.getReference().update("read", true);
                        }

                        // Update unread count in the chat document
                        try {
                            DocumentReference chatRef = firestore.collection("chats").document(chatId);
                            Map<String, Object> update = new HashMap<>();
                            Map<String, Object> unreadCounts = new HashMap<>();
                            unreadCounts.put(currentUserId, 0);
                            update.put("unreadCount", unreadCounts);
                            chatRef.set(update, SetOptions.merge());
                        } catch (Exception e) {
                            Log.e(TAG, "Error updating unread count", e);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error marking messages as read", e);
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in markMessagesAsRead", e);
        }
    }

    private void sendMessage() {
        try {
            if (auth.getCurrentUser() == null) {
                Log.e(TAG, "Current user is null");
                Toast.makeText(ChatActivity.this, "Authentication error", Toast.LENGTH_SHORT).show();
                return;
            }

            if (chatId == null) {
                Log.e(TAG, "Chat ID is null");
                Toast.makeText(ChatActivity.this, "Chat error", Toast.LENGTH_SHORT).show();
                return;
            }

            String messageText = messageInput.getText().toString().trim();

            if (messageText.isEmpty()) {
                return;
            }

            // Clear input
            messageInput.setText("");

            // Create message object
            Map<String, Object> message = new HashMap<>();
            message.put("senderId", currentUserId);
            message.put("text", messageText);
            message.put("timestamp", new Date());
            message.put("read", false);

            // Add message to Firestore
            firestore.collection("chats").document(chatId)
                    .collection("messages")
                    .add(message)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "Message sent successfully with ID: " + documentReference.getId());

                        try {
                            // Create update for chat document
                            Map<String, Object> chatUpdate = new HashMap<>();
                            chatUpdate.put("lastMessage", messageText);
                            chatUpdate.put("lastMessageSenderId", currentUserId);
                            chatUpdate.put("lastMessageTimestamp", new Date());

                            // Update the chat document directly using set with merge option
                            firestore.collection("chats").document(chatId)
                                    .set(chatUpdate, SetOptions.merge())
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Last message updated successfully to: " + messageText);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error updating last message", e);
                                        Toast.makeText(ChatActivity.this, "Failed to update chat info", Toast.LENGTH_SHORT).show();
                                    });

                            // Handle unread count in a separate operation
                            DocumentReference chatRef = firestore.collection("chats").document(chatId);
                            chatRef.get()
                                    .addOnSuccessListener(snapshot -> {
                                        try {
                                            Map<String, Object> updates = new HashMap<>();
                                            Map<String, Object> unreadCounts;

                                            if (snapshot.contains("unreadCount") && snapshot.get("unreadCount") instanceof Map) {
                                                unreadCounts = new HashMap<>((Map<String, Object>) snapshot.get("unreadCount"));
                                            } else {
                                                unreadCounts = new HashMap<>();
                                            }

                                            // Get current count or default to 0
                                            Long currentCount = 0L;
                                            if (unreadCounts.containsKey(otherUserId)) {
                                                Object countObj = unreadCounts.get(otherUserId);
                                                if (countObj instanceof Long) {
                                                    currentCount = (Long) countObj;
                                                } else if (countObj instanceof Integer) {
                                                    currentCount = ((Integer) countObj).longValue();
                                                } else if (countObj instanceof Double) {
                                                    currentCount = ((Double) countObj).longValue();
                                                }
                                            }

                                            // Increment count
                                            unreadCounts.put(otherUserId, currentCount + 1);

                                            // Ensure current user has an entry
                                            if (!unreadCounts.containsKey(currentUserId)) {
                                                unreadCounts.put(currentUserId, 0L);
                                            }

                                            updates.put("unreadCount", unreadCounts);

                                            // Set with merge for safety
                                            chatRef.set(updates, SetOptions.merge())
                                                    .addOnSuccessListener(aVoid -> {
                                                        Log.d(TAG, "Unread count updated successfully");
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Log.e(TAG, "Error updating unread count", e);
                                                    });
                                        } catch (Exception e) {
                                            Log.e(TAG, "Error processing unread count", e);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error getting chat document for unread count update", e);
                                    });
                        } catch (Exception e) {
                            Log.e(TAG, "Error updating chat metadata", e);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error sending message", e);
                        Toast.makeText(ChatActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in sendMessage", e);
            Toast.makeText(this, "Error sending message", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Mark chat as read when activity resumes
        try {
            if (chatId != null && currentUserId != null) {
                ChatsFragment.markChatAsRead(chatId, currentUserId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error marking chat as read in onResume", e);
        }
    }
}