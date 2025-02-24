package com.example.sharedfood.chat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Manages chat operations, including creating chats, sending messages, and retrieving messages.
 */
public class ChatManager {
    private FirebaseFirestore db;

    /**
     * Constructor initializes Firestore instance.
     */
    public ChatManager() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Creates a new chat between the current user and the post owner.
     *
     * @param chatId        Unique ID for the chat.
     * @param currentUserId ID of the current user.
     * @param postUserId    ID of the post owner.
     */
    public void createChat(String chatId, String currentUserId, String postUserId) {
        // Create a list of participants (current user and post owner)
        List<String> participants = new ArrayList<>();
        participants.add(currentUserId);  // Current user
        participants.add(postUserId);     // Post owner

        // Get the current Firebase timestamp
        Timestamp timestamp = Timestamp.now();

        // Create a Chat object with participants and timestamp
        Chat chat = new Chat(chatId, participants, timestamp);

        // Save the chat to Firestore
        db.collection("chats").document(chatId).set(chat)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Chat was successfully created
                        System.out.println("Chat created successfully!");
                    } else {
                        // Failed to create chat
                        System.out.println("Failed to create chat.");
                    }
                });
    }

    /**
     * Sends a message in a chat.
     *
     * @param chatId      The chat ID where the message should be sent.
     * @param userId      The sender's user ID.
     * @param messageText The content of the message.
     */
    public void sendMessage(String chatId, String userId, String messageText) {
        // Generate a unique ID for the message
        String messageId = UUID.randomUUID().toString();

        // Get the current Firebase timestamp
        Timestamp timestamp = Timestamp.now();

        // Create the message object
        Message message = new Message(messageId, userId, messageText, timestamp);

        // Save the message in Firestore under the chat's messages collection
        db.collection("chats").document(chatId).collection("messages").document(messageId).set(message);

        // Update the lastUpdated field in the chat document
        db.collection("chats").document(chatId)
                .update("lastUpdated", timestamp);
    }

    /**
     * Retrieves messages from a chat in ascending order by timestamp.
     *
     * @param chatId   The chat ID from which to retrieve messages.
     * @param callback Callback to handle the retrieved messages.
     */
    public void getMessages(String chatId, OnCompleteListener<QuerySnapshot> callback) {
        db.collection("chats").document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING) // Sort by timestamp
                .get()
                .addOnCompleteListener(callback);
    }
}
