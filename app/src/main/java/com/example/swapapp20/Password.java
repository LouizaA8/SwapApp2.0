package com.example.swapapp20;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class Password extends AppCompatActivity {
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        auth = FirebaseAuth.getInstance();

        // Retrieve email from Intent
        String email = getIntent().getStringExtra("email");

        EditText passwordEditText = findViewById(R.id.textPassword);
        ImageView signUpButton = findViewById(R.id.nextPageBasicInfo);

        // Sign-Up Button Listener
        signUpButton.setOnClickListener(v -> {
            String password = passwordEditText.getText().toString().trim();

            // Validate password
            if (password.isEmpty()) {
                passwordEditText.setError("Password is required");
                return;
            }
            if (password.length() < 6) {
                passwordEditText.setError("Password must be at least 6 characters");
                return;
            }

            // Call the sign-up method
            signUpWithFirebase(email, password);
        });
    }
    private void signUpWithFirebase(String email, String password) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Sign-up successful
                        Toast.makeText(this, "Sign-Up Successful", Toast.LENGTH_SHORT).show();

                        // Navigate to Main Activity or another screen
                        Intent intent = new Intent(Password.this, BasicInformation.class);
                        startActivity(intent);
                        finish(); // Close the current activity
                    } else {
                        // Handle errors
                        String errorMessage = task.getException() != null
                                ? task.getException().getMessage()
                                : "Sign-Up Failed";
                        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}