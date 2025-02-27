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

public class Size extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_size);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        String selectedChipText = getIntent().getStringExtra("selectedChipText");
        String name = getIntent().getStringExtra("name");
        String age = getIntent().getStringExtra("age");
        String selectedStyleText = getIntent().getStringExtra("selectedStyleChips");

        ChipGroup sizeChips = findViewById(R.id.sizePicker);
        ImageView sizePickerButton = findViewById(R.id.nextLocation);
        sizePickerButton.setOnClickListener(v -> {
            int selectedSizeId = sizeChips.getCheckedChipId();

            if (selectedSizeId != View.NO_ID) {
                Chip selectedChip = findViewById(selectedSizeId);
                String selectedSizeText = selectedChip.getText().toString();

                Intent toHeight = new Intent(Size.this, Height.class);
                toHeight.putExtra("selectedStyleChips", selectedStyleText);
                toHeight.putExtra("name", name);
                toHeight.putExtra("age", age);
                toHeight.putExtra("selectedChipText", selectedChipText);
                toHeight.putExtra("selectedSizeText", selectedSizeText);

                startActivity(toHeight);
            } else {
                Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
