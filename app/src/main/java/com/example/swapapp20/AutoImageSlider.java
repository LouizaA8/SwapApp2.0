package com.example.swapapp20;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

public class AutoImageSlider extends AppCompatActivity {
    private ViewPager2 viewPager;
    private ImageSliderAdapter adapter;
    private final Handler SliderHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_auto_image_slider);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



        viewPager = findViewById(R.id.viewPager);

        // Add your image resources here
        List<Integer> imageList = new ArrayList<>();
        imageList.add(R.drawable.im);
        imageList.add(R.drawable.freepik__expand);
        imageList.add(R.drawable.freepik);

        adapter = new ImageSliderAdapter(imageList);
        viewPager.setAdapter(adapter);

        // Auto-scroll setup
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                SliderHandler.removeCallbacks(sliderRunnable);
                SliderHandler.postDelayed(sliderRunnable, 3000); // 3 seconds delay
            }
        });

        Button signUpButton = findViewById(R.id.signupButton);
        signUpButton.setOnClickListener(v -> {
            // Inside AutoImageSlider.java (after user signs in or signs up)
            SharedPreferences sharedPreferences = getSharedPreferences("SwapAppPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isFirstTime", false); // Mark user as NOT first-time
            editor.apply();

            Intent signUpPage = new Intent(AutoImageSlider.this, email.class);
            startActivity(signUpPage);
            finish();
        });
        Button signInButton = findViewById(R.id.signInButton);
        signInButton.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = getSharedPreferences("SwapAppPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isFirstTime", false); // Mark user as NOT first-time
            editor.apply();
            Intent signInPage = new Intent(AutoImageSlider.this, SignIn.class);
            startActivity(signInPage);
            finish();
        });


    }

    private Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            int currentItem = viewPager.getCurrentItem();
            int nextItem = (currentItem + 1) % adapter.getItemCount();
            viewPager.setCurrentItem(nextItem, true);
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        SliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SliderHandler.postDelayed(sliderRunnable, 3000);
    }
}