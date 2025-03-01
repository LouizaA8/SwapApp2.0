package com.example.swapapp20;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class ChatsFragment extends Fragment {
    private static final String TAG = "ChatsFragment";

    private RecyclerView recyclerViewChats;
    private TextView emptyChatsText;
    private ChatAdapter chatAdapter;
    private List<ChatModel> chatsList;

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    public ChatsFragment() {
        // Required empty public constructor
    }

    public static ChatsFragment newInstance() {
        return new ChatsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize UI components
        recyclerViewChats = view.findViewById(R.id.recyclerViewChats);
        emptyChatsText = view.findViewById(R.id.emptyChatsText);

        // Setup RecyclerView
        chatsList = new ArrayList<>();
        chatAdapter = new ChatAdapter(requireContext(), chatsList);
        recyclerViewChats.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewChats.setAdapter(chatAdapter);

        // Load existing chats
        loadChats();

        return view;
    }

    private void loadChats() {
        if (auth.getCurrentUser() == null) return;

        String currentUserId = auth.getCurrentUser().getUid();

        firestore.collection("chats")
                .whereArrayContains("participants", currentUserId)
                .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading chats", error);
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Error loading chats", Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }

                    if (value != null) {
                        chatsList.clear();

                        for (DocumentSnapshot doc : value.getDocuments()) {
                            ChatModel chat = doc.toObject(ChatModel.class);
                            if (chat != null) {
                                chat.setId(doc.getId());
                                chatsList.add(chat);
                            }
                        }

                        chatAdapter.notifyDataSetChanged();

                        // Update UI to show empty state if needed
                        if (chatsList.isEmpty()) {
                            emptyChatsText.setVisibility(View.VISIBLE);
                            recyclerViewChats.setVisibility(View.GONE);
                        } else {
                            emptyChatsText.setVisibility(View.GONE);
                            recyclerViewChats.setVisibility(View.VISIBLE);
                        }

                        Log.d(TAG, "Loaded " + chatsList.size() + " chats");
                    }
                });
    }

    // This method creates a new chat when users match
    // It should be called from the match creation logic
    public static void createChatForMatch(Match match) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String user1 = match.getUser1Id();
        String user2 = match.getUser2Id();

        // First check if a chat already exists between these users
        db.collection("chats")
                .whereArrayContains("participants", user1)
                .get()
                .addOnSuccessListener(queryDocuments -> {
                    boolean chatExists = false;

                    for (DocumentSnapshot doc : queryDocuments) {
                        List<String> participants = (List<String>) doc.get("participants");
                        if (participants != null && participants.contains(user2)) {
                            chatExists = true;
                            break;
                        }
                    }

                    if (!chatExists) {
                        // Create a new chat document
                        Map<String, Object> chatData = new HashMap<>();
                        List<String> participants = new ArrayList<>();
                        participants.add(user1);
                        participants.add(user2);

                        chatData.put("participants", participants); // Changed from "users" to "participants"
                        chatData.put("matchId", match.getId());
                        chatData.put("createdAt", new Date());
                        chatData.put("lastMessageTimestamp", new Date());
                        chatData.put("lastMessage", "Say hello to your new match!");
                        chatData.put("lastMessageSenderId", "system");
                        chatData.put("unreadCount", new HashMap<String, Integer>() {{
                            put(user1, 1);
                            put(user2, 1);
                        }});

                        // Add to Firestore
                        db.collection("chats")
                                .add(chatData)
                                .addOnSuccessListener(documentReference -> {
                                    String chatId = documentReference.getId();
                                    Log.d(TAG, "Chat created with ID: " + chatId);

                                    // Add a system message to start the conversation
                                    Map<String, Object> message = new HashMap<>();
                                    message.put("senderId", "system");
                                    message.put("text", "You've matched! Say hello and start chatting.");
                                    message.put("timestamp", new Date());
                                    message.put("read", false);

                                    db.collection("chats")
                                            .document(chatId)
                                            .collection("messages")
                                            .add(message);

                                    // Update the match document with the chat ID
                                    db.collection("matches")
                                            .document(match.getId())
                                            .update("chatId", chatId);
                                })
                                .addOnFailureListener(e -> Log.e(TAG, "Error creating chat", e));
                    } else {
                        Log.d(TAG, "Chat already exists between these users");
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error checking existing chats", e));
    }
    // To be called when the user views a chat to reset unread count
    public static void markChatAsRead(String chatId, String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference chatRef = db.collection("chats").document(chatId);

        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(chatRef);
            Map<String, Integer> unreadCount = (Map<String, Integer>) snapshot.get("unreadCount");

            if (unreadCount != null) {
                unreadCount.put(userId, 0);
                transaction.update(chatRef, "unreadCount", unreadCount);
            }

            return null;
        }).addOnFailureListener(e -> Log.e(TAG, "Error marking chat as read", e));
    }
}