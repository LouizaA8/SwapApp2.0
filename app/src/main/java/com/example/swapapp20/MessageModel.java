package com.example.swapapp20;

import java.util.Date;

public class MessageModel {
    private String id;
    private String senderId;
    private String text;
    private Date timestamp;
    private boolean read;

    // Empty constructor required for Firestore
    public MessageModel() {
    }

    // Constructor with parameters
    public MessageModel(String senderId, String text, Date timestamp, boolean read) {
        this.senderId = senderId;
        this.text = text;
        this.timestamp = timestamp;
        this.read = read;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}