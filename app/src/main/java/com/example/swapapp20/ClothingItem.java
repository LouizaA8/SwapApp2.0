package com.example.swapapp20;

public class ClothingItem {
    private String id;
    private String imageUrl;
    private String description;
    private boolean isLiked;

    public ClothingItem() {
        // Default constructor required for Firestore
    }

    public ClothingItem(String id, String imageUrl, String description, boolean isLiked) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.description = description;
        this.isLiked = isLiked;
    }

    public String getId() {
        return id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }
}
