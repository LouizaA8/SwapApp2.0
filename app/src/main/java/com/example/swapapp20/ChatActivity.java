package com.example.swapapp20;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
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

        // Get data from intent
        chatId = getIntent().getStringExtra("chatId");
        otherUserId = getIntent().getStringExtra("otherUserId");

// If otherUserId is null, try the alternative key
        if (otherUserId == null) {
            otherUserId = getIntent().getStringExtra("userId");
        }

        if (chatId == null || otherUserId == null) {
            Toast.makeText(this, "Error loading chat", Toast.LENGTH_SHORT).show();
            finish();
            Log.d(TAG, "Intent extras - chatId: " + chatId + ", otherUserId: " + otherUserId
                    + ", userId: " + getIntent().getStringExtra("userId"));
            return;

        }

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        // Initialize UI components
        toolbar = findViewById(R.id.chatToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

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
        ChatsFragment.markChatAsRead(chatId, currentUserId);

        // Set send button click listener
        sendButton.setOnClickListener(v -> sendMessage());

        // Back button listener
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadUserDetails() {
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
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading user details", e);
                    userName.setText("User");
                    userImage.setImageResource(R.drawable.profile);
                });
    }

    private void loadMessages() {
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
                            MessageModel message = doc.toObject(MessageModel.class);
                            if (message != null) {
                                message.setId(doc.getId());
                                messagesList.add(message);
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
    }

    private void markMessagesAsRead() {
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
                    DocumentReference chatRef = firestore.collection("chats").document(chatId);
                    firestore.runTransaction(transaction -> {
                        DocumentSnapshot snapshot = transaction.get(chatRef);
                        Map<String, Integer> unreadCount = (Map<String, Integer>) snapshot.get("unreadCount");

                        if (unreadCount != null) {
                            unreadCount.put(currentUserId, 0);
                            transaction.update(chatRef, "unreadCount", unreadCount);
                        }

                        return null;
                    });
                });
    }

    private void sendMessage() {
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
                    // Update last message in chat document
                    Map<String, Object> chatUpdate = new HashMap<>();
                    chatUpdate.put("lastMessage", messageText);
                    chatUpdate.put("lastMessageSenderId", currentUserId);
                    chatUpdate.put("lastMessageTimestamp", new Date());

                    // Update unread count for the other user
                    DocumentReference chatRef = firestore.collection("chats").document(chatId);
                    firestore.runTransaction(transaction -> {
                        DocumentSnapshot snapshot = transaction.get(chatRef);
                        Map<String, Integer> unreadCount = (Map<String, Integer>) snapshot.get("unreadCount");

                        if (unreadCount != null) {
                            Integer count = unreadCount.get(otherUserId);
                            if (count != null) {
                                unreadCount.put(otherUserId, count + 1);
                            } else {
                                unreadCount.put(otherUserId, 1);
                            }

                            chatUpdate.put("unreadCount", unreadCount);
                        }

                        transaction.update(chatRef, chatUpdate);
                        return null;
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error sending message", e);
                    Toast.makeText(ChatActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Mark chat as read when activity resumes
        if (chatId != null && currentUserId != null) {
            ChatsFragment.markChatAsRead(chatId, currentUserId);
        }
    }
}