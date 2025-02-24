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

/**
 * Activity for handling chat interactions between users.
 * It displays messages, allows sending new messages, and updates in real-time.
 */
public class ChatActivity extends AppCompatActivity {
    private RecyclerView messagesRecyclerView; // RecyclerView for displaying messages
    private MessageAdapter messageAdapter; // Adapter for message display
    private List<Message> messagesList; // List of messages
    private ChatManager chatManager; // Handles Firebase chat operations
    private EditText messageInput; // Input field for messages
    private ImageButton sendButton; // Button to send messages
    private String chatId; // Chat identifier
    private String currentUserId; // User ID of the current user

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Retrieve chatId and currentUserId from intent
        chatId = getIntent().getStringExtra("chatId");
        currentUserId = getIntent().getStringExtra("currentUserId");

        // Initialize ChatManager and RecyclerView
        chatManager = new ChatManager();
        messagesList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messagesList);
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messagesRecyclerView.setAdapter(messageAdapter);

        // Initialize message input field and send button
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        // Load messages from Firestore
        loadMessages();

        // Set up click listener for the send button
        sendButton.setOnClickListener(v -> sendMessage());
    }

    /**
     * Loads messages from Firestore for the current chat.
     */
    private void loadMessages() {
        chatManager.getMessages(chatId, task -> {
            if (task.isSuccessful()) {
                QuerySnapshot documentSnapshot = task.getResult();
                messagesList.clear();
                for (com.google.firebase.firestore.QueryDocumentSnapshot document : documentSnapshot) {
                    String messageId = document.getId();
                    String messageText = document.getString("messageText");
                    Timestamp timestamp = document.getTimestamp("timestamp"); // Retrieve timestamp
                    String userId = document.getString("userId");

                    // Create a new Message object and add it to the list
                    Message message = new Message(messageId, userId, messageText, timestamp);
                    messagesList.add(message);
                }

                // Update UI with new messages
                messageAdapter.notifyDataSetChanged();
                messagesRecyclerView.scrollToPosition(messagesList.size() - 1); // Scroll to the latest message
            } else {
                Toast.makeText(ChatActivity.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Sends a new message to Firestore.
     */
    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (!messageText.isEmpty()) {
            chatManager.sendMessage(chatId, currentUserId, messageText);
            messageInput.setText(""); // Clear message input field

            // Add the new message to the list
            Timestamp timestamp = Timestamp.now(); // Get the current timestamp
            Message newMessage = new Message(
                    "",  // ID will be generated in Firestore
                    currentUserId,
                    messageText,
                    timestamp // Use Firebase timestamp
            );
            messagesList.add(newMessage);
            messageAdapter.notifyItemInserted(messagesList.size() - 1);
            messagesRecyclerView.scrollToPosition(messagesList.size() - 1); // Scroll to the latest message
        } else {
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
        }
    }
}
