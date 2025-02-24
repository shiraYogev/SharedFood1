package com.example.sharedfood.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sharedfood.R;
import com.example.sharedfood.chat.Chat;
import com.example.sharedfood.chat.MyChatsAdapter;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity class for displaying and managing the user's chats.
 * Implements a listener for chat selection.
 */
public class MyChatsActivity extends AppCompatActivity implements MyChatsAdapter.OnChatClickListener {

    private RecyclerView chatListRecyclerView;
    private MyChatsAdapter myChatsAdapter;
    private List<Chat> chatList;
    private FirebaseFirestore db;
    private String currentUserId;

    /**
     * Initializes the activity, sets up the RecyclerView, and loads the user's chats.
     * @param savedInstanceState The saved state of the activity, if any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_chats);

        // Initialize Firebase Firestore and get current user ID
        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Set up RecyclerView and adapter
        chatListRecyclerView = findViewById(R.id.chatListRecyclerView);
        chatList = new ArrayList<>();
        myChatsAdapter = new MyChatsAdapter(chatList, this);

        chatListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatListRecyclerView.setAdapter(myChatsAdapter);

        // Load the user's chats from Firebase
        loadUserChats();
    }

    /**
     * Loads the chats where the current user is a participant from Firestore.
     */
    private void loadUserChats() {
        db.collection("chats")
                .whereArrayContains("participants", currentUserId)  // Find all chats where the user is a participant
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        chatList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Extract chat details from the document
                            String chatId = document.getId();
                            List<String> participants = (List<String>) document.get("participants");
                            Timestamp lastUpdated = document.getTimestamp("lastUpdated"); // Retrieve the timestamp

                            // Create a Chat object for each conversation
                            Chat chat = new Chat(chatId, participants, lastUpdated);
                            chatList.add(chat);
                        }

                        // Update UI based on whether chats were found
                        if (chatList.isEmpty()) {
                            Toast.makeText(MyChatsActivity.this, "אין צ'אטים זמינים", Toast.LENGTH_SHORT).show();
                        } else {
                            myChatsAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Toast.makeText(MyChatsActivity.this, "שגיאה בטעינת הצ'אטים", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Handles a chat click event by navigating to the ChatActivity with the selected chat ID.
     * @param chatId The ID of the clicked chat.
     */
    @Override
    public void onChatClick(String chatId) {
        // Navigate to the chat activity
        Intent intent = new Intent(MyChatsActivity.this, ChatActivity.class);
        intent.putExtra("chatId", chatId);
        intent.putExtra("currentUserId", currentUserId);
        startActivity(intent);
    }
}