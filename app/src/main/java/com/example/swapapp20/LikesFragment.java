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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class LikesFragment extends Fragment {
    private static final String TAG = "LikesFragment";
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<NotificationModel> notifications;
    private RecyclerView recyclerViewMatches;
    private TextView emptyMatchesText;
    private MatchAdapter matchAdapter;
    private List<Match> matchesList;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_likes, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewLikes);
        recyclerViewMatches = view.findViewById(R.id.recyclerViewMatches);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        notifications = new ArrayList<>();
        adapter = new NotificationAdapter(requireContext(), notifications);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        loadNotifications();

        return view;
    }

    private void loadNotifications() {
        if (auth.getCurrentUser() == null) return;

        String currentUserId = auth.getCurrentUser().getUid();

        firestore.collection("notifications")
                .whereEqualTo("receiverId", currentUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading notifications", error);
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Error loading notifications", Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }

                    if (value != null) {
                        notifications.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Log.d(TAG, "Document ID: " + doc.getId());
                            Log.d(TAG, "Document Data: " + doc.getData());

                            NotificationModel notification = doc.toObject(NotificationModel.class);
                            if (notification != null) {
                                notification.setId(doc.getId());  // Set document ID properly
                                notifications.add(notification);
                            } else {
                                Log.e(TAG, "Null notification object for doc: " + doc.getId());
                            }
                        }
                        adapter.notifyDataSetChanged();
                        Log.d(TAG, "Loaded " + notifications.size() + " notifications");
                    }
                });
    }


}

