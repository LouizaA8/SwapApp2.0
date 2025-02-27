package com.example.swapapp20;

import android.content.Intent;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;

public class Birthday extends AppCompatActivity {
    private DatePicker datePicker;
    private TextView ageTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_birthday);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        datePicker = findViewById(R.id.birthday_datePicker);
        ageTextView = findViewById(R.id.user_age);

        // Set a listener to calculate age when the date is changed
        datePicker.init(
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
                (view, year, monthOfYear, dayOfMonth) -> calculateAge(year, monthOfYear, dayOfMonth)
        );

        ImageView BirthdayPicker = findViewById(R.id.nextGenderPicker);
        String name = getIntent().getStringExtra("name");

        BirthdayPicker.setOnClickListener(v -> {
            String age = ageTextView.getText().toString().trim();
            Intent toGenderPick = new Intent(this, GenderPicker.class);
            toGenderPick.putExtra("age", age);
            toGenderPick.putExtra("name", name);
            startActivity(toGenderPick);
        });


    }
    void calculateAge(int year, int month, int day) {
        // Get the current date
        Calendar today = Calendar.getInstance();
        int currentYear = today.get(Calendar.YEAR);
        int currentMonth = today.get(Calendar.MONTH);
        int currentDay = today.get(Calendar.DAY_OF_MONTH);

        // Calculate age
        int age = currentYear - year;

        // Adjust for the current month and day
        if (currentMonth < month || (currentMonth == month && currentDay < day)) {
            age--;
        }

        // Display the age in the TextView
        ageTextView.setText( age );
    }
}