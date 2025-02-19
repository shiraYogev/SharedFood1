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

public class ChatManager {
    private FirebaseFirestore db;

    public ChatManager() {
        db = FirebaseFirestore.getInstance();
    }

    // יצירת שיחה חדשה
    public void createChat(String chatId, String currentUserId, String postUserId) {
        // יצירת רשימת משתתפים (המשתמש הנוכחי והמפרסם)
        List<String> participants = new ArrayList<>();
        participants.add(currentUserId);  // המשתמש הנוכחי
        participants.add(postUserId);     // המפרסם של הפוסט

        // יצירת Timestamp של Firebase
        Timestamp timestamp = Timestamp.now();  // משתמש ב-Timestamp עכשיו

        // יצירת אובייקט Chat עם משתתפים ו-Timestamp
        Chat chat = new Chat(chatId, participants, timestamp);

        // שמירה ב-Firestore
        db.collection("chats").document(chatId).set(chat)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // צ'אט נוצר בהצלחה
                        System.out.println("Chat created successfully!");
                    } else {
                        // שגיאה ביצירת הצ'אט
                        System.out.println("Failed to create chat.");
                    }
                });
    }

    // שליחת הודעה חדשה
    public void sendMessage(String chatId, String userId, String messageText) {
        String messageId = UUID.randomUUID().toString();

        // יצירת Timestamp של Firebase
        Timestamp timestamp = Timestamp.now();  // משתמש ב-Timestamp עכשיו

        // יצירת ההודעה
        Message message = new Message(messageId, userId, messageText, timestamp);

        // שמירה ב-Firestore
        db.collection("chats").document(chatId).collection("messages").document(messageId).set(message);

        // עדכון שדה lastUpdated בשיחה
        db.collection("chats").document(chatId)
                .update("lastUpdated", timestamp);
    }

    // קריאת הודעות
    public void getMessages(String chatId, OnCompleteListener<QuerySnapshot> callback) {
        db.collection("chats").document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING) // מיון לפי זמן
                .get()
                .addOnCompleteListener(callback);
    }
}
