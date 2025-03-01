package com.example.swapapp20;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class ChatModel {
    private String id;
    private List<String> participants;
    private String matchId;
    private Date createdAt;
    private Date lastMessageTimestamp;
    private String lastMessage;
    private String lastMessageSenderId;
    private Map<String, Integer> unreadCount;

    // Empty constructor for Firestore
    public ChatModel() {
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    public void setLastMessageTimestamp(Date lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastMessageSenderId() {
        return lastMessageSenderId;
    }

    public void setLastMessageSenderId(String lastMessageSenderId) {
        this.lastMessageSenderId = lastMessageSenderId;
    }

    public Map<String, Integer> getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Map<String, Integer> unreadCount) {
        this.unreadCount = unreadCount;
    }

    // Helper method to get the other user's ID
    public String getOtherUserId(String currentUserId) {
        if (participants != null && participants.size() == 2) {
            if (participants.get(0).equals(currentUserId)) {
                return participants.get(1);
            } else {
                return participants.get(0);
            }
        }
        return "";
    }
}