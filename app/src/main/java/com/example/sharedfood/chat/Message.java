package com.example.sharedfood.chat;

import com.google.firebase.Timestamp;

public class Message {
    private String messageId;
    private String userId;
    private String messageText;
    private Timestamp timestamp;  // שדה timestamp ישמר כ-String

    // Constructor
    public Message(String messageId, String userId, String messageText, Timestamp timestamp) {
        this.messageId = messageId;
        this.userId = userId;
        this.messageText = messageText;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
