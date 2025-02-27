package com.example.swapapp20;

public class UserProfile {
    private String userId;
    private String name;
    private String age;
    private String chipText;  // "chipText" corresponds to gender
    private String size;
    private String location;
    private String height;
    private String style;
    private String coverPhotoUrl; // Match with Firestore field name

    public UserProfile() {
        // Required empty constructor for Firestore
    }

    public UserProfile(String userId, String name, String age, String chipText,
                       String size, String location, String height, String style, String coverPhotoUrl) {
        this.userId = userId;
        this.name = name;
        this.age = age;
        this.chipText = chipText;
        this.size = size;
        this.location = location;
        this.height = height;
        this.style = style;
        this.coverPhotoUrl = coverPhotoUrl;
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getAge() { return age; }
    public String getChipText() { return chipText; }  // Gender field
    public String getSize() { return size; }
    public String getLocation() { return location != null ? location : "Unknown"; } // Handle null locations
    public String getHeight() { return height; }
    public String getStyle() { return style; }
    public String getCoverPhotoUrl() { return coverPhotoUrl; }
}
