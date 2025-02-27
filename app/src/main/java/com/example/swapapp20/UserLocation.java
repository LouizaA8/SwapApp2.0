package com.example.swapapp20;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class UserLocation extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap gMap;
    private static final int FINE_PERMISSION_CODE = 1;
    private Location currentLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private SupportMapFragment mapFragment;
    private String selectedLocationName = "";

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

        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyD-FScI4gRMwooxGrL-Wy5FxflV7GobqpU");
        }

        // Initialize Map Fragment
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Initialize Location Provider
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();

        // Setup Autocomplete Fragment
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);


        if (autocompleteFragment != null) {
            autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    if (place.getLatLng() != null) {

                        gMap.clear();
                        gMap.addMarker(new MarkerOptions().position(place.getLatLng()).title(place.getName()));
                        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 20));
                        getAddressFromLatLng(place.getLatLng());
                    }
                }

                @Override
                public void onError(@NonNull com.google.android.gms.common.api.Status status) {
                    Toast.makeText(UserLocation.this, "Error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        // Data from Previous Act
        String selectedChipText = getIntent().getStringExtra("selectedChipText");
        String name = getIntent().getStringExtra("name");
        String age = getIntent().getStringExtra("age");
        String selectedStyleText = getIntent().getStringExtra("selectedStyleChips");
        String selectedSizeText = getIntent().getStringExtra("selectedSizeText");
        String selectedHeight = getIntent().getStringExtra("selectedHeight");


        // Next Activity Intent

        ImageView locationPicker = findViewById(R.id.nextProfileSetUp);
        locationPicker.setOnClickListener(v -> {
            Intent toProfileSetUp = new Intent(UserLocation.this, CoverPhoto.class);
            toProfileSetUp.putExtra("selectedStyleChips", selectedStyleText);
            toProfileSetUp.putExtra("name", name);
            toProfileSetUp.putExtra("age", age);
            toProfileSetUp.putExtra("selectedChipText", selectedChipText);
            toProfileSetUp.putExtra("selectedSizeText", selectedSizeText);
            toProfileSetUp.putExtra("SelectedHeight", selectedHeight);
            toProfileSetUp.putExtra("UserLocation",selectedLocationName);
            startActivity(toProfileSetUp);
        });
    }

    private void getAddressFromLatLng(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                selectedLocationName = address.getAddressLine(0);  // Full address

            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Unable to get address", Toast.LENGTH_SHORT).show();
        }}

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            return;
        }

        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    if (gMap != null) {
                        updateMapLocation(currentLocation);
                    }
                } else {
                    Toast.makeText(UserLocation.this, "Could not get current location", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateMapLocation(Location location) {
        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        gMap.clear();
        gMap.addMarker(new MarkerOptions().position(userLatLng).title("My Location"));
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;

        if (currentLocation != null) {
            updateMapLocation(currentLocation);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Toast.makeText(this, "Location Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
