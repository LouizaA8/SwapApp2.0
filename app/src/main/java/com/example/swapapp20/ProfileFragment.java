package com.example.swapapp20;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";
    private static final String ARG_USER_ID = "userId";

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private String currentUserId;
    private String displayedUserId;

    private ImageView profileImage;
    private TextView profileName, postCount;
    private TextView profileGender, profileSize, profileLocation, profileHeight, profileAge, profileStyle;
    private RecyclerView postsRecyclerView;
    private ProfilePostsAdapter postsAdapter;
    private List<Post> userPosts;

    public static ProfileFragment newInstance(String userId) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        Button uploadButton = view.findViewById(R.id.uploadButton);
        uploadButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), Upload.class);
            startActivity(intent);
        });
        initializeFirebase();
        initializeViews(view);
        setupRecyclerView();
        loadProfileData();

        return view;
    }

    private void initializeFirebase() {
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUserId = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        displayedUserId = getArguments() != null ? getArguments().getString(ARG_USER_ID, currentUserId) : currentUserId;

        Log.d(TAG, "Displayed User ID: " + displayedUserId);
    }

    private void initializeViews(View view) {
        profileImage = view.findViewById(R.id.profileImage);
        profileName = view.findViewById(R.id.profileName);
        postCount = view.findViewById(R.id.postCount);

        // Initialize individual profile attribute views
        profileGender = view.findViewById(R.id.profileGender);
        profileSize = view.findViewById(R.id.profileSize);
        profileLocation = view.findViewById(R.id.profileLocation);
        profileHeight = view.findViewById(R.id.profileHeight);
        profileAge = view.findViewById(R.id.profileAge);
        profileStyle = view.findViewById(R.id.profileStyle);

        postsRecyclerView = view.findViewById(R.id.postsRecyclerView);
        Button editProfileButton = view.findViewById(R.id.editProfileButton);

        // Show or hide the edit button based on user
        editProfileButton.setVisibility(displayedUserId.equals(currentUserId) ? View.VISIBLE : View.GONE);
        editProfileButton.setOnClickListener(v -> openEditProfileDialog());
    }

    private void setupRecyclerView() {
        userPosts = new ArrayList<>();
        postsAdapter = new ProfilePostsAdapter(requireContext(), userPosts);
        postsRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 3)); // 3 columns for grid
        postsRecyclerView.setAdapter(postsAdapter);
    }

    private void loadProfileData() {
        fetchProfileData();
        fetchUserPosts();
    }

    private void fetchProfileData() {
        firestore.collection("users").document(displayedUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "User Data: " + documentSnapshot.getData());
                        updateUIWithProfileData(documentSnapshot);
                    } else {
                        Log.e(TAG, "User document does not exist");
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching profile data", e));
    }

    private void updateUIWithProfileData(DocumentSnapshot documentSnapshot) {
        String name = documentSnapshot.getString("name");
        String profileImageUrl = documentSnapshot.getString("coverPhotoUrl");

        // Set individual attribute values
        String gender = documentSnapshot.getString("chipText");
        String size = documentSnapshot.getString("size");
        String location = documentSnapshot.getString("location");
        String height = documentSnapshot.getString("height");
        String age = documentSnapshot.getString("age");
        String style = documentSnapshot.getString("style");

        // Log the values for debugging
        Log.d(TAG, "Name: " + name);
        Log.d(TAG, "Gender: " + gender);
        Log.d(TAG, "Size: " + size);
        Log.d(TAG, "Location: " + location);
        Log.d(TAG, "Height: " + height);
        Log.d(TAG, "Age: " + age);
        Log.d(TAG, "Style: " + style);
        Log.d(TAG, "Profile Image URL: " + profileImageUrl);

        // Set text values with fallbacks for null values
        profileName.setText(name != null ? name : "Unknown User");
        profileGender.setText(gender != null ? gender : "");
        profileSize.setText(size != null ? size : "");
        profileLocation.setText(location != null ? location : "");
        profileHeight.setText(height != null ? height : "");
        profileAge.setText(age != null ? age : "");
        profileStyle.setText(style != null ? style : "");

        // Load profile image
        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
            Glide.with(requireContext())
                    .load(profileImageUrl)
                    .placeholder(R.drawable.person)
                    .error(R.drawable.person)
                    .into(profileImage);
        } else {
            profileImage.setImageResource(R.drawable.person);
        }
    }

    private void fetchUserPosts() {
        firestore.collection("posts")
                .whereEqualTo("userId", displayedUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userPosts.clear();
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        Post post = snapshot.toObject(Post.class);
                        if (post != null) {
                            userPosts.add(post);
                        }
                    }
                    postCount.setText(String.valueOf(userPosts.size())); // Update post count
                    postsAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching user posts", e));
    }

    private void openEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_edit_profile, null);
        builder.setView(dialogView);
        builder.setTitle("Edit Profile");

        // Initialize dialog components
        EditText editName = dialogView.findViewById(R.id.editName);
        EditText editStyle = dialogView.findViewById(R.id.editStyle);
        EditText editSize = dialogView.findViewById(R.id.editSize);
        EditText editHeight = dialogView.findViewById(R.id.editHeight);
        EditText editLocation = dialogView.findViewById(R.id.editLocation);
        EditText editAge = dialogView.findViewById(R.id.editAge);
        EditText editGender = dialogView.findViewById(R.id.editGender);

        // Pre-fill the fields with current data
        firestore.collection("users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        editName.setText(documentSnapshot.getString("name"));
                        editStyle.setText(documentSnapshot.getString("style"));
                        editSize.setText(documentSnapshot.getString("size"));
                        editHeight.setText(documentSnapshot.getString("height"));
                        editLocation.setText(documentSnapshot.getString("location"));
                        editAge.setText(documentSnapshot.getString("Age")); // Match Firestore field name
                        editGender.setText(documentSnapshot.getString("chipText"));
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error pre-filling profile data", e));

        // Dialog buttons
        builder.setPositiveButton("Save", (dialog, which) -> {
            String updatedName = editName.getText().toString().trim();
            String updatedStyle = editStyle.getText().toString().trim();
            String updatedSize = editSize.getText().toString().trim();
            String updatedHeight = editHeight.getText().toString().trim();
            String updatedLocation = editLocation.getText().toString().trim();
            String updatedAge = editAge.getText().toString().trim();
            String updatedGender = editGender.getText().toString().trim();

            // Update Firestore
            firestore.collection("users").document(currentUserId)
                    .update(
                            "name", updatedName,
                            "style", updatedStyle,
                            "size", updatedSize,
                            "height", updatedHeight,
                            "location", updatedLocation,
                            "Age", updatedAge, // Match Firestore field name
                            "chipText", updatedGender
                    )
                    .addOnSuccessListener(aVoid -> fetchProfileData())
                    .addOnFailureListener(e -> Log.e(TAG, "Error updating profile", e));
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }
}