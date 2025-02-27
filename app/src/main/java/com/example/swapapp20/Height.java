package com.example.swapapp20;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Height extends AppCompatActivity {
    private NumberPicker heightPicker;
    private TextView heightText;
    private int selectedHeight = 170;  // Default height

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_height);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get previous data from Intent
        String selectedChipText = getIntent().getStringExtra("selectedChipText");
        String name = getIntent().getStringExtra("name");
        String age = getIntent().getStringExtra("age");
        String selectedStyleText = getIntent().getStringExtra("selectedStyleChips");
        String selectedSizeText = getIntent().getStringExtra("selectedSizeText");

        // Initialize Views
        heightText = findViewById(R.id.actualHeight);
        heightPicker = findViewById(R.id.heightPicker);
        ImageView heightButton = findViewById(R.id.nextLocation1);

        // Set NumberPicker Range
        heightPicker.setMinValue(100);
        heightPicker.setMaxValue(250);
        heightPicker.setValue(selectedHeight);  // Default value

        // Set default height in TextView
        heightText.setText(selectedHeight + " cm");

        // Set NumberPicker Change Listener
        heightPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            selectedHeight = newVal;
            heightText.setText(newVal + " cm");
        });

        // Button Click Listener to Pass Data
        heightButton.setOnClickListener(v -> {
            Intent toLocation = new Intent(Height.this, UserLocation.class);
            toLocation.putExtra("selectedStyleChips", selectedStyleText);
            toLocation.putExtra("name", name);
            toLocation.putExtra("age", age);
            toLocation.putExtra("selectedChipText", selectedChipText);
            toLocation.putExtra("selectedSizeText", selectedSizeText);
            toLocation.putExtra("SelectedHeight", selectedHeight );  // Pass selected height
            startActivity(toLocation);
        });
    }
}
