package com.example.sharedfood.chat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ChatManager {
    private FirebaseFirestore db;

    public ChatManager() {
        db = FirebaseFirestore.getInstance();
    }

    // יצירת שיחה חדשה
    public void createChat(String chatId, List<String> participants) {
        Chat chat = new Chat(chatId, participants, getCurrentTimestamp());
        db.collection("chats").document(chatId).set(chat);
    }

    // עדכון משתתפים בשיחה
    public void updateChatParticipants(String chatId, List<String> participants) {
        db.collection("chats").document(chatId)
                .update("participants", participants, "lastUpdated", getCurrentTimestamp());
    }

    // שליחת הודעה חדשה
    public void sendMessage(String chatId, String userId, String messageText) {
        String messageId = UUID.randomUUID().toString();
        String timestamp = getCurrentTimestamp();

        Message message = new Message(messageId, userId, messageText, timestamp);
        db.collection("chats").document(chatId).collection("messages").document(messageId).set(message);

        // עדכון שדה lastUpdated בשיחה
        db.collection("chats").document(chatId)
                .update("lastUpdated", timestamp);
    }

    // קריאת הודעות
    public void getMessages(String chatId, OnCompleteListener<QuerySnapshot> callback) {
        db.collection("chats").document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(callback);
    }

    private String getCurrentTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(new Date());
    }
}

