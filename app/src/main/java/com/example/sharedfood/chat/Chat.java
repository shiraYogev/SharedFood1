package com.example.sharedfood.chat;

import java.util.List;

public class Chat {
    private String chatId;
    private List<String> participants;
    private String lastUpdated;

    // Constructor
    public Chat(String chatId, List<String> participants, String lastUpdated) {
        this.chatId = chatId;
        this.participants = participants;
        this.lastUpdated = lastUpdated;
    }

    // Getters and Setters
    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}

