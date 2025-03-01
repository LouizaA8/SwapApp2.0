package com.example.swapapp20;

public class NotificationModel {
    private String id;
    private String imageUrl;
    private String message;
    private String postId;
    private String receiverId;
    private String senderId;
    private long timestamp;
    private String senderUsername;
    private String type;

    public NotificationModel() {} // Required for Firestore

    public NotificationModel(String imageUrl, String message, String postId,
                             String receiverId, String senderId, String senderUsername, String type) {
        this.imageUrl = imageUrl;
        this.message = message;
        this.postId = postId;
        this.receiverId = receiverId;
        this.senderId = senderId;
        this.timestamp = System.currentTimeMillis();
        this.senderUsername = senderUsername;
        this.type = type;
    }

    // Getters and Setters
    public String getSenderUsername() { return senderUsername; }
    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

}