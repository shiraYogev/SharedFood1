package com.example.sharedfood.activities;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sharedfood.R;
import com.example.sharedfood.chat.ChatManager;
import com.example.sharedfood.chat.Message;
import com.example.sharedfood.chat.MessageAdapter;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView messagesRecyclerView;
    private MessageAdapter messageAdapter;
    private List<Message> messagesList;
    private ChatManager chatManager;
    private EditText messageInput;
    private ImageButton sendButton;
    private String chatId;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // קבלת chatId ו-currentUserId מתוך Intent
        chatId = getIntent().getStringExtra("chatId");
        currentUserId = getIntent().getStringExtra("currentUserId");

        // אתחול של ChatManager ו-RecyclerView
        chatManager = new ChatManager();
        messagesList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messagesList);
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messagesRecyclerView.setAdapter(messageAdapter);

        // אתחול של שדה ההודעה ושל כפתור השליחה
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        // טעינת ההודעות מהצ'אט
        loadMessages();

        // הגדרת פעולה לכפתור לשליחת הודעה
        sendButton.setOnClickListener(v -> sendMessage());
    }

    // טעינת ההודעות מה-Firestore
    private void loadMessages() {
        chatManager.getMessages(chatId, task -> {
            if (task.isSuccessful()) {
                QuerySnapshot documentSnapshot = task.getResult();
                messagesList.clear();
                for (com.google.firebase.firestore.QueryDocumentSnapshot document : documentSnapshot) {
                    String messageId = document.getId();
                    String messageText = document.getString("messageText");
                    Timestamp timestamp = document.getTimestamp("timestamp"); // עכשיו מקבלים Timestamp
                    String userId = document.getString("userId");

                    // יצירת Message חדש והוספתו לרשימה
                    Message message = new Message(messageId, userId, messageText, timestamp);
                    messagesList.add(message);
                }

                // עדכון ה-UI
                messageAdapter.notifyDataSetChanged();
                messagesRecyclerView.scrollToPosition(messagesList.size() - 1);  // גלילה למטה לאחר טעינת ההודעות
            } else {
                Toast.makeText(ChatActivity.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // שליחת הודעה
    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (!messageText.isEmpty()) {
            chatManager.sendMessage(chatId, currentUserId, messageText);
            messageInput.setText("");  // ניקוי שדה ההודעה

            // הוספת ההודעה החדשה לרשימה
            Timestamp timestamp = Timestamp.now(); // משתמש ב-Timestamp של Firebase
            Message newMessage = new Message(
                    "",  // id חדש ייווצר ב-Firestore
                    currentUserId,
                    messageText,
                    timestamp  // Timestamp ישירות
            );
            messagesList.add(newMessage);
            messageAdapter.notifyItemInserted(messagesList.size() - 1);
            messagesRecyclerView.scrollToPosition(messagesList.size() - 1);  // גלילה למטה
        } else {
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
        }
    }
}
