package com.example.sharedfood.chat;
public class Message {
    private String messageId;
    private String userId;
    private String messageText;
    private String timestamp;

    // Constructor
    public Message(String messageId, String userId, String messageText, String timestamp) {
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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
