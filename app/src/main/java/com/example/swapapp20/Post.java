package com.example.swapapp20;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Post {
    private String postId;
    private String imageUrl;
    private String caption;
    private String hashtags;
    private String userId;
    private long timestamp;
    private int likes;
    private boolean isPublic; // Optional for post visibility
    private List<String> taggedUsers; // Optional for tagging users
    private List<String> comments; // Optional for supporting comments
    private String senderUsername;

    public Post() {
        // Required empty constructor for Firestore
    }

    public Post(String imageUrl, String caption, String hashtags, String userId, long timestamp, String senderUsername) {
        this.imageUrl = imageUrl;
        this.caption = caption;

        this.userId = userId;
        this.timestamp = timestamp;
        this.likes = 0; // Default value
        this.isPublic = true; // Default visibility
        this.senderUsername = senderUsername;
    }

    public String getUsername() { return senderUsername; }
    public void setUsername(String senderUsername) { this.senderUsername = senderUsername; }

    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }



    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }

    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }

    public List<String> getTaggedUsers() { return taggedUsers; }
    public void setTaggedUsers(List<String> taggedUsers) { this.taggedUsers = taggedUsers; }

    public List<String> getComments() { return comments; }
    public void setComments(List<String> comments) { this.comments = comments; }

    public String getFormattedTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
