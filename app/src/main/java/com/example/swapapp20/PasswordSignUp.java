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

public class PasswordSignUp extends AppCompatActivity {
 private FirebaseAuth auth1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_password_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        auth1 = FirebaseAuth.getInstance();

        // Retrieve email from Intent
        String email = getIntent().getStringExtra("emailSignIn");

        EditText passwordSignIn = findViewById(R.id.textPasswordSignIn);
        ImageView goHomePage = findViewById(R.id.nextPageHome);

        // Sign-Up Button Listener
        goHomePage.setOnClickListener(v -> {
            String password = passwordSignIn.getText().toString().trim();

            // Validate password
            if (password.isEmpty()) {
                passwordSignIn.setError("Password is required");
                return;
            }
            if (password.length() < 6) {
                passwordSignIn.setError("Password must be at least 6 characters");
                return;
            }

            // Call the sign-up method
            signInWithFirebase(email,password);
        });
    }
    private void signInWithFirebase(String email, String password) {
        auth1.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Sign-up successful
                        Toast.makeText(this, "Sign-In Successful", Toast.LENGTH_SHORT).show();

                        // Navigate to Main Activity or another screen
                        Intent intent = new Intent(PasswordSignUp.this, MainActivity.class);
                        startActivity(intent);
                        finish(); // Close the current activity
                    } else {
                        // Handle errors
                        String errorMessage = task.getException() != null
                                ? task.getException().getMessage()
                                : "Sign-in Failed";
                        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}