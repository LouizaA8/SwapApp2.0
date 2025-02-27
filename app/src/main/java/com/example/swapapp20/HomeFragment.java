package com.example.swapapp20;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Button uploadButton = view.findViewById(R.id.uploadButton);
        uploadButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), Upload.class);
            startActivity(intent);
        });

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        postList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), postList);

        recyclerView.setAdapter(postAdapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        loadPosts();

        return view;
    }

    private void loadPosts() {
        db.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(getContext(), "Failed to load posts", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    postList.clear();
                    if (queryDocumentSnapshots != null) {
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            Post post = document.toObject(Post.class);
                            if (post != null) {
                                post.setPostId(document.getId());
                                postList.add(post);
                            }
                        }
                    }
                    postAdapter.notifyDataSetChanged();
                });
    }

}
