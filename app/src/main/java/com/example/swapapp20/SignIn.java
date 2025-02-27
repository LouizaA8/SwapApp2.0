package com.example.swapapp20;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SignIn extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        EditText emailSignIn = findViewById(R.id.emailSignIn);
        ImageView nextPagePasswordSignIn = findViewById(R.id.nextPagePasswordSignIn);

        // Next Button Listener
        nextPagePasswordSignIn.setOnClickListener(v -> {
            String email = emailSignIn.getText().toString().trim();

            // Validate email
            if (email.isEmpty()) {
                emailSignIn.setError("Email is required");
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailSignIn.setError("Enter a valid email address");
                return;
            }

            // Pass email to second activity
            Intent intent = new Intent(SignIn.this, PasswordSignUp.class);
            intent.putExtra("emailSignIn", email);
            startActivity(intent);
        });
    }
}