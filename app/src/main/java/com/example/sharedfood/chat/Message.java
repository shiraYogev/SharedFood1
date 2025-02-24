package com.example.sharedfood.chat;

import com.google.firebase.Timestamp;

/**
 * Represents a message in the chat system.
 */
public class Message {
    private String messageId;  // Unique identifier for the message
    private String userId;     // ID of the user who sent the message
    private String messageText; // The content of the message
    private Timestamp timestamp;  // Timestamp indicating when the message was sent

    /**
     * Constructor to initialize a message object.
     *
     * @param messageId   Unique ID of the message.
     * @param userId      ID of the sender.
     * @param messageText The text content of the message.
     * @param timestamp   The timestamp when the message was created.
     */
    public Message(String messageId, String userId, String messageText, Timestamp timestamp) {
        this.messageId = messageId;
        this.userId = userId;
        this.messageText = messageText;
        this.timestamp = timestamp;
    }

    /**
     * Gets the message ID.
     *
     * @return The unique message ID.
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Sets the message ID.
     *
     * @param messageId The unique message ID to set.
     */
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    /**
     * Gets the user ID of the sender.
     *
     * @return The sender's user ID.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the user ID of the sender.
     *
     * @param userId The sender's user ID to set.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Gets the message text.
     *
     * @return The content of the message.
     */
    public String getMessageText() {
        return messageText;
    }

    /**
     * Sets the message text.
     *
     * @param messageText The message content to set.
     */
    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    /**
     * Gets the timestamp of when the message was sent.
     *
     * @return The timestamp of the message.
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp of when the message was sent.
     *
     * @param timestamp The timestamp to set.
     */
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
