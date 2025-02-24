package com.example.sharedfood.chat;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class Chat {
    private String chatId; // Unique identifier for the chat
    private List<String> participants; // List of participant user IDs
    private Timestamp lastUpdated; // Timestamp of the last update to the chat

    // Constructor
    public Chat(String chatId, List<String> participants, Timestamp lastUpdated) {
        this.chatId = chatId;
        this.participants = participants;
        this.lastUpdated = lastUpdated;
    }

    // Getters and Setters
    public String getChatId() {
        return chatId; // Return chat ID
    }

    public void setChatId(String chatId) {
        this.chatId = chatId; // Set chat ID
    }

    public List<String> getParticipants() {
        return participants; // Return list of participants
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants; // Set list of participants
    }

    public Timestamp getLastUpdated() {
        return lastUpdated; // Return last updated timestamp
    }

    public void setLastUpdated(Timestamp lastUpdated) {
        this.lastUpdated = lastUpdated; // Set last updated timestamp
    }

    // Function to get the ID of the other user in the chat
    public String getOtherUserId() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get current user ID
        return participants != null && participants.size() > 1 // Check if there are enough participants
                ? participants.get(0).equals(currentUserId) ? participants.get(1) : participants.get(0) // Return the other user's ID
                : null; // Return null if there are not enough participants
    }
}
