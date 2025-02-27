package com.example.swapapp20;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.content.ContentResolver;
import android.os.AsyncTask;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.InputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

public class CoverPhoto extends AppCompatActivity {
    private Uri coverPhotoUri;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private Cloudinary cloudinary;
    private boolean isUploading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cover_photo);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeFirebase();
        initializeCloudinary();
        setupUI();
    }

    private void initializeFirebase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void initializeCloudinary() {
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dneo2modd",
                "api_key", "112493554574789",
                "api_secret", "t7Bil7eAGkVQDxXizgrLMSlMWCc"
        ));
    }

    private void setupUI() {
        ImageView coverButton = findViewById(R.id.coverButton);
        Button uploadPhoto = findViewById(R.id.uploadPhotoB);

        coverButton.setOnClickListener(v -> {
            if (!isUploading) {
                saveUserData();
            }
        });

        uploadPhoto.setOnClickListener(v -> ImagePicker.with(CoverPhoto.this)
                .maxResultSize(1080, 1080)
                .compress(1024)
                .start());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            coverPhotoUri = data.getData();
            if (coverPhotoUri != null) {
                ImageView photoImg = findViewById(R.id.placeholderImg);
                photoImg.setImageURI(coverPhotoUri);
            } else {
                Toast.makeText(this, "Failed to get image", Toast.LENGTH_SHORT).show();
            }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUserData() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please sign in to continue", Toast.LENGTH_SHORT).show();
            return;
        }

        if (coverPhotoUri == null) {
            Toast.makeText(this, "Please upload a cover photo", Toast.LENGTH_SHORT).show();
            return;
        }

        isUploading = true;
        showLoadingState(true);

        // Get the intent extras
        String selectedChipText = getIntent().getStringExtra("selectedChipText");
        String name = getIntent().getStringExtra("name");
        String age = getIntent().getStringExtra("age");
        String selectedStyleText = getIntent().getStringExtra("selectedStyleChips");
        String selectedSizeText = getIntent().getStringExtra("selectedSizeText");
        String selectedHeight = String.valueOf(getIntent().getIntExtra("SelectedHeight", 170)); // Default to 170 if no value is passed
        String selectedLocationName = getIntent().getStringExtra("selectedLocationName");

        new ImageUploadTask(
                currentUser.getUid(),
                selectedChipText,
                name,
                age,
                selectedStyleText,
                selectedSizeText,
                selectedHeight,
                selectedLocationName
        ).execute(coverPhotoUri);
    }

    private class ImageUploadTask extends AsyncTask<Uri, Void, String> {
        private final String userId;
        private final String chipText;
        private final String name;
        private final String age;
        private final String style;
        private final String size;
        private final String height;
        private final String location;
        private Exception exception;

        ImageUploadTask(String userId, String chipText, String name, String age,
                        String style, String size, String height, String location) {
            this.userId = userId;
            this.chipText = chipText;
            this.name = name;
            this.age = age;
            this.style = style;
            this.size = size;
            this.height = height;
            this.location = location;
        }

        @Override
        protected String doInBackground(Uri... uris) {
            try {
                File tempFile = createTempFileFromUri(uris[0]);
                Map uploadResult = cloudinary.uploader().upload(tempFile, ObjectUtils.emptyMap());
                tempFile.delete();
                return (String) uploadResult.get("secure_url");
            } catch (Exception e) {
                exception = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(String imageUrl) {
            if (imageUrl != null) {
                saveUserToFirestore(imageUrl);
            } else {
                handleError(exception);
            }
        }

        private void saveUserToFirestore(String imageUrl) {
            Map<String, Object> userData = new HashMap<>();
            userData.put("chipText", chipText); // for gender
            userData.put("name", name);
            userData.put("age", age);
            userData.put("style", style);
            userData.put("size", size);
            userData.put("height", height);
            userData.put("location", location);
            userData.put("coverPhotoUrl", imageUrl);

            db.collection("users").document(userId)
                    .set(userData)
                    .addOnSuccessListener(aVoid -> {
                        showLoadingState(false);
                        isUploading = false;
                        Toast.makeText(CoverPhoto.this, "Profile Saved Successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(CoverPhoto.this, MainActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        showLoadingState(false);
                        isUploading = false;
                        handleError(e);
                    });
        }
    }

    private File createTempFileFromUri(Uri uri) throws Exception {
        ContentResolver contentResolver = getContentResolver();
        InputStream inputStream = contentResolver.openInputStream(uri);
        File tempFile = File.createTempFile("image", ".jpg", getCacheDir());

        FileOutputStream outputStream = new FileOutputStream(tempFile);
        byte[] buffer = new byte[8192];
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        outputStream.close();
        inputStream.close();
        return tempFile;
    }

    private void showLoadingState(boolean isLoading) {
        // Implement loading UI state (progress bar, disable buttons, etc.)
        findViewById(R.id.coverButton).setEnabled(!isLoading);
        findViewById(R.id.uploadPhotoB).setEnabled(!isLoading);
        // Add progress bar visibility toggle here
    }

    private void handleError(Exception e) {
        isUploading = false;
        showLoadingState(false);
        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }
}