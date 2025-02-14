package com.example.sharedfood;

import com.example.sharedfood.chat.*;  // ייבוא כל המחלקות שבחבילה chat


import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private String chatId;
    private FirebaseFirestore db;
    private EditText messageInput;
    private ImageButton sendButton;
    private RecyclerView messagesRecyclerView;
    private MessageAdapter messagesAdapter;
    private List<Message> messagesList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        db = FirebaseFirestore.getInstance();
        chatId = getIntent().getStringExtra("chatId");
        Log.d("ChatActivity", "Chat opened with chatId: " + chatId);

        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);

        // הגדרת RecyclerView
        messagesAdapter = new MessageAdapter(messagesList);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messagesRecyclerView.setAdapter(messagesAdapter);

        sendButton.setOnClickListener(v -> sendMessage());

        loadMessages();
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (!TextUtils.isEmpty(messageText)) {
            Map<String, Object> msgData = new HashMap<>();
            msgData.put("text", messageText);
            msgData.put("timestamp", System.currentTimeMillis());

            db.collection("chats").document(chatId).collection("messages")
                    .add(msgData)
                    .addOnSuccessListener(documentReference -> messageInput.setText(""))
                    .addOnFailureListener(e -> {
                        Toast.makeText(ChatActivity.this, "Message failed to send", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void loadMessages() {
        db.collection("chats").document(chatId).collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        for (DocumentChange change : value.getDocumentChanges()) {
                            if (change.getType() == DocumentChange.Type.ADDED) {
                                QueryDocumentSnapshot doc = change.getDocument();
                                Message message = new Message(
                                        doc.getString("sender"),        // פרמטר נוסף (נניח שם השולח)
                                        doc.getString("receiver"),      // פרמטר נוסף (נניח שם המקבל)
                                        doc.getString("text"),          // הטקסט של ההודעה
                                        String.valueOf(doc.getLong("timestamp")) // תאריך ושעה (להמיר ל-String אם צריך)
                                );

                                messagesList.add(message);
                            }
                        }
                        messagesAdapter.notifyDataSetChanged();
                    }
                });
    }
}
