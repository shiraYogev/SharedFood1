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

public class MyChatsActivity extends AppCompatActivity implements MyChatsAdapter.OnChatClickListener {

    private RecyclerView chatListRecyclerView;
    private MyChatsAdapter myChatsAdapter;
    private List<Chat> chatList;
    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_chats);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        chatListRecyclerView = findViewById(R.id.chatListRecyclerView);
        chatList = new ArrayList<>();
        myChatsAdapter = new MyChatsAdapter(chatList, this);

        chatListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatListRecyclerView.setAdapter(myChatsAdapter);

        // טעינת הצ'אטים מה-Firebase
        loadUserChats();
    }

    private void loadUserChats() {
        db.collection("chats")
                .whereArrayContains("participants", currentUserId)  // מצא את כל הצ'אטים שהמשתמש חלק מהם
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        chatList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String chatId = document.getId();
                            List<String> participants = (List<String>) document.get("participants");
                            Timestamp lastUpdated = document.getTimestamp("lastUpdated"); // השגת ה-Timestamp

                            // יצירת אובייקט Chat לכל שיחה
                            Chat chat = new Chat(chatId, participants, lastUpdated);
                            chatList.add(chat);
                        }

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


    @Override
    public void onChatClick(String chatId) {
        // מעבר לאקטיביטי של הצ'אט
        Intent intent = new Intent(MyChatsActivity.this, ChatActivity.class);
        intent.putExtra("chatId", chatId);
        intent.putExtra("currentUserId", currentUserId);
        startActivity(intent);
    }
}
