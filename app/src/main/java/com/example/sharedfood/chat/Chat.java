package com.example.sharedfood.chat;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class Chat {
    private String chatId;
    private List<String> participants;
    private Timestamp lastUpdated;

    // Constructor
    public Chat(String chatId, List<String> participants, Timestamp lastUpdated) {
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

    public Timestamp getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Timestamp lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    // פונקציה לקבלת מזהה המשתמש השני
    public String getOtherUserId() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        return participants != null && participants.size() > 1
                ? participants.get(0).equals(currentUserId) ? participants.get(1) : participants.get(0)
                : null; // במקרה שאין מספיק משתתפים
    }
}

