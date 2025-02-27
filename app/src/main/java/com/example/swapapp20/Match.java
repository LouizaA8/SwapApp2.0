package com.example.swapapp20;

import com.google.firebase.firestore.Exclude;

import java.util.Date;
import java.util.List;

public class Match {
    private String id;
    private String user1Id;
    private String user2Id;
    private String post1Id;
    private String post2Id;
    private Date timestamp;
    private boolean isActive;
    private List<String> users; // Array for querying purposes

    // Empty constructor required for Firestore
    public Match() {
    }

    public Match(String user1Id, String user2Id, String post1Id, String post2Id) {
        this.user1Id = user1Id;
        this.user2Id = user2Id;
        this.post1Id = post1Id;
        this.post2Id = post2Id;
        this.isActive = true;
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser1Id() {
        return user1Id;
    }

    public void setUser1Id(String user1Id) {
        this.user1Id = user1Id;
    }

    public String getUser2Id() {
        return user2Id;
    }

    public void setUser2Id(String user2Id) {
        this.user2Id = user2Id;
    }

    public String getPost1Id() {
        return post1Id;
    }

    public void setPost1Id(String post1Id) {
        this.post1Id = post1Id;
    }

    public String getPost2Id() {
        return post2Id;
    }

    public void setPost2Id(String post2Id) {
        this.post2Id = post2Id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    // Helper method to get the other user's ID
    @Exclude
    public String getOtherUserId(String currentUserId) {
        if (user1Id.equals(currentUserId)) {
            return user2Id;
        } else {
            return user1Id;
        }
    }
}