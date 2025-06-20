package com.example.swapapp20;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class Upload extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private ImageView imageView;
    private EditText captionInput;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private boolean isComingFromCoverPhoto = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        // Check if coming from CoverPhoto activity
        if (getIntent().hasExtra("fromCoverPhoto")) {
            isComingFromCoverPhoto = getIntent().getBooleanExtra("fromCoverPhoto", false);
        }

        // Initialize Cloudinary MediaManager
        try {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", "dneo2modd");
            config.put("api_key", "112493554574789");
            config.put("api_secret", "t7Bil7eAGkVQDxXizgrLMSlMWCc");
            MediaManager.init(this, config);
        } catch (Exception e) {
            Log.e("CloudinaryInit", "Failed to initialize MediaManager: " + e.getMessage());
            Toast.makeText(this, "Failed to initialize upload service", Toast.LENGTH_SHORT).show();
        }

        imageView = findViewById(R.id.imageView);
        captionInput = findViewById(R.id.captionInput);
        Button pickImageButton = findViewById(R.id.pickImageButton);
        Button uploadButton = findViewById(R.id.uploadButton);
        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recyclerView);

        db = FirebaseFirestore.getInstance();

        pickImageButton.setOnClickListener(v -> openFileChooser());
        uploadButton.setOnClickListener(v -> uploadImageToCloudinary());
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imageView.setImageURI(imageUri);
        } else {
            Toast.makeText(this, "Failed to select image", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImageToCloudinary() {
        if (imageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE); // Show progress

        MediaManager.get().upload(imageUri)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Log.d("CloudinaryUpload", "Upload started");
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        Log.d("CloudinaryUpload", "Uploading: " + bytes + "/" + totalBytes);
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = resultData.get("secure_url").toString();
                        Log.d("CloudinaryUpload", "Upload Success: " + imageUrl);
                        savePostToFirestore(imageUrl);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Log.e("CloudinaryUpload", "Upload Error: " + error.getDescription());
                        Toast.makeText(Upload.this, "Upload failed: " + error.getDescription(), Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        Log.w("CloudinaryUpload", "Upload Rescheduled: " + error.getDescription());
                    }
                }).dispatch();
    }

    private void savePostToFirestore(String imageUrl) {
        String caption = captionInput.getText().toString().trim();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        Map<String, Object> post = new HashMap<>();
        post.put("imageUrl", imageUrl);
        post.put("caption", caption);
        post.put("timestamp", System.currentTimeMillis());
        post.put("userId", userId);
        post.put("likes", 0);

        db.collection("posts").add(post)
                .addOnSuccessListener(documentReference -> {
                    String postId = documentReference.getId();
                    db.collection("posts").document(postId).update("postId", postId);
                    Log.d("FirestoreUpload", "Post saved with ID: " + documentReference.getId());
                    Toast.makeText(Upload.this, "Post uploaded!", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);

                    // Clear input fields and reset image
                    captionInput.setText("");
                    imageView.setImageURI(null);
                    imageUri = null;

                    // If coming from CoverPhoto, navigate to home page after upload
                    if (isComingFromCoverPhoto) {
                        Intent intent = new Intent(Upload.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }

                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreUpload", "Upload failed: " + e.getMessage());
                    Toast.makeText(Upload.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        progressBar.setVisibility(View.GONE);
    }
}