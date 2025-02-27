package com.example.swapapp20;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class PostDetailFragment extends Fragment {
    private static final String TAG = "PostDetailFragment";
    private String postId;
    private FirebaseFirestore db;
    private RecyclerView recyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get post ID from arguments
        if (getArguments() != null) {
            postId = getArguments().getString("postId");
            Log.d(TAG, "Post ID received: " + postId);
        } else {
            Log.e(TAG, "No arguments provided to fragment");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_post_detail, container, false);

        // Find the RecyclerView in the inflated layout
        recyclerView = view.findViewById(R.id.detailRecyclerView);
        if (recyclerView == null) {
            Log.e(TAG, "detailRecyclerView not found in layout");
            return view;
        }

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Load post data
        loadPost();

        return view;
    }

    private void loadPost() {
        if (postId == null || postId.isEmpty()) {
            Log.e(TAG, "No post ID provided");
            Toast.makeText(requireContext(), "Error loading post", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Loading post with ID: " + postId);

        db.collection("posts").document(postId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Post post = documentSnapshot.toObject(Post.class);
                        if (post != null) {
                            post.setPostId(documentSnapshot.getId());
                            displayPost(post);
                            Log.d(TAG, "Post loaded successfully: " + post.getPostId());
                        } else {
                            Log.e(TAG, "Failed to convert document to Post object");
                            Toast.makeText(requireContext(), "Error loading post", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Post document does not exist");
                        Toast.makeText(requireContext(), "Post not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading post", e);
                    Toast.makeText(requireContext(), "Error loading post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void displayPost(Post post) {
        if (recyclerView == null) {
            Log.e(TAG, "RecyclerView is null in displayPost");
            return;
        }

        // Create a list with just this one post
        List<Post> singlePostList = new ArrayList<>();
        singlePostList.add(post);

        // Use the PostAdapter to display the post
        PostAdapter postAdapter = new PostAdapter(requireContext(), singlePostList);
        recyclerView.setAdapter(postAdapter);

        Log.d(TAG, "Post displayed in RecyclerView");
    }
}