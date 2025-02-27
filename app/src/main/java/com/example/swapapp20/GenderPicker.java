package com.example.swapapp20;

import android.content.Intent;
import android.os.Bundle;
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

public class GenderPicker extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gender_picker);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        String name = getIntent().getStringExtra("name");
        String age = getIntent().getStringExtra("age");

        ChipGroup genderChips = findViewById(R.id.genderChips);
        ImageView genderPicker = findViewById(R.id.nextPageStyleDescription);

        genderPicker.setOnClickListener(v -> {
            int selectedChipId = genderChips.getCheckedChipId();

            if (selectedChipId != View.NO_ID) {
                Chip selectedChip = findViewById(selectedChipId);
                String selectedText = selectedChip.getText().toString();

            Intent toStyleDesc = new Intent(GenderPicker.this, StyleDescription.class);
            toStyleDesc.putExtra("name", name);
            toStyleDesc.putExtra("age",age);
            toStyleDesc.putExtra("selectedChipText", selectedText);
            startActivity(toStyleDesc);
        }
    else {
            Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show();
            }

        });
}
}