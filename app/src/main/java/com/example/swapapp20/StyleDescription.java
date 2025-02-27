package com.example.swapapp20;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class StyleDescription extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_style_description);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        String selectedChipText = getIntent().getStringExtra("selectedChipText");
        String name = getIntent().getStringExtra("name");
        String age = getIntent().getStringExtra("age");
        ChipGroup styleChips = findViewById(R.id.styleChips);
        ImageView StylePicker = findViewById(R.id.nextSize);
        StylePicker.setOnClickListener(v -> {
            List<String> selectedChips = new ArrayList<>();

            for (int i = 0; i < styleChips.getChildCount(); i++) {
                Chip chip = (Chip) styleChips.getChildAt(i);
                if (chip.isChecked()) {
                    selectedChips.add(chip.getText().toString());
                }
            }

            if (!selectedChips.isEmpty()) {
                String selectedStyleText = TextUtils.join(", ", selectedChips); // Convert List to String

                Intent toSize = new Intent(StyleDescription.this, Size.class);
                  toSize.putExtra("selectedStyleChips", selectedStyleText);
                  toSize.putExtra("name", name);
                  toSize.putExtra("age",age);
                  toSize.putExtra("selectedChipText", selectedChipText);
                  startActivity(toSize);}

            else {
                Toast.makeText(this, "Please select at least one option", Toast.LENGTH_SHORT).show();
             }
    });
        }
}