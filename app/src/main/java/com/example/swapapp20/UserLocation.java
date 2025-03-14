package com.example.swapapp20;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class UserLocation extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap gMap;
    private SupportMapFragment mapFragment;
    private String selectedLocationName = "";
    private EditText searchEditText;
    private Button searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_location);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Map Fragment
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Initialize search components
        searchEditText = findViewById(R.id.location_search_edit_text);
        searchButton = findViewById(R.id.search_button);

        // Set up search button click listener
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchLocation();
            }
        });

        // Data from Previous Activity
        String selectedChipText = getIntent().getStringExtra("selectedChipText");
        String name = getIntent().getStringExtra("name");
        String age = getIntent().getStringExtra("age");
        String selectedStyleText = getIntent().getStringExtra("selectedStyleChips");
        String selectedSizeText = getIntent().getStringExtra("selectedSizeText");
        String selectedHeight = getIntent().getStringExtra("selectedHeight");

        // Next Activity Intent
        ImageView locationPicker = findViewById(R.id.nextProfileSetUp);
        locationPicker.setOnClickListener(v -> {
            if (selectedLocationName.isEmpty()) {
                Toast.makeText(this, "Please select a location first", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent toProfileSetUp = new Intent(UserLocation.this, CoverPhoto.class);
            toProfileSetUp.putExtra("selectedStyleChips", selectedStyleText);
            toProfileSetUp.putExtra("name", name);
            toProfileSetUp.putExtra("age", age);
            toProfileSetUp.putExtra("selectedChipText", selectedChipText);
            toProfileSetUp.putExtra("selectedSizeText", selectedSizeText);
            toProfileSetUp.putExtra("SelectedHeight", selectedHeight);
            toProfileSetUp.putExtra("selectedLocationName", selectedLocationName);
            startActivity(toProfileSetUp);
        });
    }

    private void searchLocation() {
        String locationName = searchEditText.getText().toString();
        if (locationName.isEmpty()) {
            Toast.makeText(this, "Please enter a location", Toast.LENGTH_SHORT).show();
            return;
        }

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(locationName, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);

                // Get the location coordinates
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                // Clear previous markers and add a new one
                gMap.clear();
                gMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(address.getAddressLine(0)));

                // Move camera to the selected location
                gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

                // Save the selected location name
                selectedLocationName = address.getAddressLine(0);

                Toast.makeText(this, "Location found: " + selectedLocationName, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Geocoding error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Allow user to select a location by tapping on the map
    private void setupMapClickListener() {
        gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                // Clear previous markers
                gMap.clear();

                // Add a marker at the tapped location
                gMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title("Selected Location"));

                // Get the address for the selected location
                getAddressFromLatLng(latLng);
            }
        });
    }

    private void getAddressFromLatLng(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                selectedLocationName = address.getAddressLine(0);  // Full address
                Toast.makeText(this, "Selected: " + selectedLocationName, Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Unable to get address", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;

        // Set default map position
        LatLng defaultLocation = new LatLng(34.0522, -118.2437);  // Los Angeles
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10));

        // Setup click listener for map interactions
        setupMapClickListener();

        Toast.makeText(this, "Enter a location or tap on the map", Toast.LENGTH_LONG).show();
    }
}