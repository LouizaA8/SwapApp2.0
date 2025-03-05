package com.example.swapapp20;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PostDetailFragment extends Fragment {
    private static final String TAG = "PostDetailFragment";
    private static final String ARG_USER_ID = "userId";
    private static final String ARG_START_POSITION = "startPosition";

    private String userId;
    private int startPosition;
    private ViewPager2 viewPager;
    private PostDetailPagerAdapter pagerAdapter;
    private List<Post> posts;
    private FirebaseFirestore firestore;
    private ImageButton backButton;

    public interface OnPostClickListener {
        void onPostClick(int position);
    }

    public static PostDetailFragment newInstance(String userId, int startPosition) {
        PostDetailFragment fragment = new PostDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        args.putInt(ARG_START_POSITION, startPosition);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString(ARG_USER_ID);
            startPosition = getArguments().getInt(ARG_START_POSITION, 0);
        }
        firestore = FirebaseFirestore.getInstance();
        posts = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_detail, container, false);


        viewPager = view.findViewById(R.id.postDetailViewPager);
        backButton = view.findViewById(R.id.backButton);

        // Setup back button
        backButton.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        });

        // Initialize the adapter with empty list, will be populated later
        pagerAdapter = new PostDetailPagerAdapter(requireContext(), posts);
        viewPager.setAdapter(pagerAdapter);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        activity.setSupportActionBar(toolbar); // Enable back button

        // Add the MenuProvider
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.delete_menu, menu); // Inflate the menu
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.delete_button) {
                    deletePost();
                    return true;
                } else if (menuItem.getItemId() == android.R.id.home) {
                    requireActivity().getSupportFragmentManager().popBackStack(); // Handle back button
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);


        loadPosts();

        return view;


    }

    private void deletePost() {
        int currentPosition = viewPager.getCurrentItem();
        if (currentPosition < 0 || currentPosition >= posts.size()) {
            Toast.makeText(requireContext(), "Invalid post selection", Toast.LENGTH_SHORT).show();
            return;
        }

        Post postToDelete = posts.get(currentPosition);
        String postId = postToDelete.getPostId(); // Get correct post ID

        if (postId == null || postId.isEmpty()) {
            Toast.makeText(requireContext(), "Post ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    firestore.collection("posts")
                            .document(postId) // Use correct post ID
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(requireContext(), "Post deleted", Toast.LENGTH_SHORT).show();

                                // Remove from list and update adapter
                                posts.remove(currentPosition);
                                pagerAdapter.notifyDataSetChanged();

                                // If no posts left, go back
                                if (posts.isEmpty()) {
                                    requireActivity().getSupportFragmentManager().popBackStack();
                                }

                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error deleting post", e);
                                Toast.makeText(requireContext(), "Failed to delete post", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

   /* private void deletePost(Post post) {
        if (post == null || post.getPostId() == null) {
            Log.e(TAG, "Cannot delete post: post or post ID is null");
            return;
        }

        firestore.collection("posts").document(post.getPostId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Remove the post from local list
                    posts.remove(post);
                    pagerAdapter.notifyDataSetChanged();

                    // If no posts left, go back
                    if (posts.isEmpty()) {
                        getParentFragmentManager().popBackStack();
                    }

                    Log.d(TAG, "Post deleted successfully: " + post.getPostId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting post", e);
                    // Optionally show a toast or error message to the user
                });
    }*/

    private void loadPosts() {
        Log.d(TAG, "Loading posts for user: " + userId);

        firestore.collection("posts")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    posts.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Post post = document.toObject(Post.class);
                        post.setPostId(document.getId());
                        posts.add(post);
                        Log.d(TAG, "Added post: " + post.getPostId());
                    }

                    Log.d(TAG, "Loaded " + posts.size() + " posts");
                    pagerAdapter.notifyDataSetChanged();

                    // Ensure we don't exceed bounds
                    if (startPosition >= 0 && startPosition < posts.size()) {
                        viewPager.setCurrentItem(startPosition, false);
                        Log.d(TAG, "Set current item to position: " + startPosition);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading posts", e));
    }
}