package com.example.swapapp20;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;

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
    private TextView titleTextView;
    private TextView descriptionTextView;
    private List<SlideItem> slideItems;

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
        titleTextView = findViewById(R.id.sliderTitle);
        descriptionTextView = findViewById(R.id.more_info);

        // slide items with both images and text
        slideItems = new ArrayList<>();
        slideItems.add(new SlideItem(R.drawable.start_o, "Welcome to SwapApp", "The easiest way to swap, share, and discover items in your community"));
        slideItems.add(new SlideItem(R.drawable.browsing_blue, "Discover Items Around You", "Browse through thousands of items available for swap in your area"));
        slideItems.add(new SlideItem(R.drawable.taking_photo_blue, "Share What You Don't Need", "Easily upload items you no longer use and give them a new life"));
        slideItems.add(new SlideItem(R.drawable.barter_blue, "Connect & Swap", "Chat with others and arrange convenient meetups to exchange items"));

        adapter = new ImageSliderAdapter(slideItems);
        viewPager.setAdapter(adapter);

        // text views with the first slide's text
        if (!slideItems.isEmpty()) {
            updateTextViews(0);
        }

        // Auto-scroll setup
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Update text views when page changes
                updateTextViews(position);

                SliderHandler.removeCallbacks(sliderRunnable);
                SliderHandler.postDelayed(sliderRunnable, 3000); // 3 seconds delay
            }
        });

        Button signUpButton = findViewById(R.id.signupButton);
        signUpButton.setOnClickListener(v -> {
            //check whether it is users first time accessing the app
            SharedPreferences sharedPreferences = getSharedPreferences("SwapAppPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isFirstTime", false);
            editor.apply();

            Intent signUpPage = new Intent(AutoImageSlider.this, email.class);
            startActivity(signUpPage);
            finish();
        });

        Button signInButton = findViewById(R.id.signInButton);
        signInButton.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = getSharedPreferences("SwapAppPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isFirstTime", false);
            editor.apply();
            Intent signInPage = new Intent(AutoImageSlider.this, SignIn.class);
            startActivity(signInPage);
            finish();
        });
    }

    // Method to update text views based on the current slide
    private void updateTextViews(int position) {
        if (position >= 0 && position < slideItems.size()) {
            SlideItem currentSlide = slideItems.get(position);
            titleTextView.setText(currentSlide.getTitle());
            descriptionTextView.setText(currentSlide.getDescription());
        }
    }

    private Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (viewPager != null && adapter != null && adapter.getItemCount() > 0) {
                    int currentItem = viewPager.getCurrentItem();
                    int nextItem = (currentItem + 1) % adapter.getItemCount();
                    viewPager.setCurrentItem(nextItem, true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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