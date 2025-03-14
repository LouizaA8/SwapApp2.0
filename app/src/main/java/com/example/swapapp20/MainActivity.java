package com.example.swapapp20;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.swapapp20.databinding.ActivityMainBinding;
import com.google.android.material.appbar.AppBarLayout;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    private WindowInsetsControllerCompat windowInsetsController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if it's first time launch before inflating layout
        SharedPreferences sharedPreferences = getSharedPreferences("SwapAppPrefs", MODE_PRIVATE);
        boolean isFirstTime = sharedPreferences.getBoolean("isFirstTime", true);

        if (isFirstTime) {
            // Redirect to AutoImageSlider for first-time users
            startActivity(new Intent(MainActivity.this, AutoImageSlider.class));
            finish();
            return;
        }

        // Set up edge-to-edge content display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        // Initialize the window insets controller
        View decorView = getWindow().getDecorView();
        windowInsetsController = ViewCompat.getWindowInsetsController(decorView);

        // Only inflate and set up UI for returning users
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set up the toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Apply padding to ensure content doesn't go under system bars
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, windowInsets) -> {
            int statusBarHeight = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            int navigationBarHeight = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;

            // Apply padding to the bottom navigation to avoid overlap with system navigation
            binding.bottomNavigationView.setPadding(0, 0, 0, navigationBarHeight);

            // Apply padding to the toolbar to ensure content doesn't overlap with the status bar
            binding.toolbar.setPadding(
                    binding.toolbar.getPaddingLeft(),
                    statusBarHeight + 8, // Add some extra padding
                    binding.toolbar.getPaddingRight(),
                    binding.toolbar.getPaddingBottom()
            );

            return WindowInsetsCompat.CONSUMED;
        });

        // Add listener to handle status bar behavior on scroll
        binding.appBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            // When toolbar is completely collapsed, hide the status bar
            if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
                if (windowInsetsController != null) {
                    windowInsetsController.hide(WindowInsetsCompat.Type.statusBars());
                }
            } else {
                if (windowInsetsController != null) {
                    windowInsetsController.show(WindowInsetsCompat.Type.statusBars());
                }
            }
        });

        // Home fragment as default
        replaceFragment(new HomeFragment());

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.homeOpt) {
                replaceFragment(new HomeFragment());
            } else if (itemId == R.id.chatOpt) {
                replaceFragment(new ChatsFragment());
            } else if (itemId == R.id.matchesOpt) {
                replaceFragment(new MatchesFragment());
            } else if (itemId == R.id.likesOpt) {
                replaceFragment(new LikesFragment());
            } else if (itemId == R.id.profileOpt) {
                replaceFragment(new ProfileFragment());
            }

            return true;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present
        getMenuInflater().inflate(R.menu.sign_out, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.signOutButton) {
            // Handle sign out logic here
            signOut();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void signOut() {
        // Clear user session data
        SharedPreferences sharedPreferences = getSharedPreferences("SwapAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Clear appropriate user data
        editor.remove("user_id");
        editor.remove("user_token");
        // Add any other user data that needs to be cleared

        // Don't clear first-time flag as user has already seen onboarding
        editor.putBoolean("isFirstTime", false);

        editor.apply();

        // Show feedback to user
        Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show();

        // Redirect to login activity
        Intent intent = new Intent(MainActivity.this, AutoImageSlider.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }
}