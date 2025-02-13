package com.example.sharedfood;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sharedfood.R;
import com.example.sharedfood.chat.Chat;
import com.example.sharedfood.chat.MyChatsAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyChatsActivity extends AppCompatActivity {

    private RecyclerView chatListRecyclerView;
    private MyChatsAdapter chatListAdapter;
    private List<Chat> chatList;
    private FirebaseFirestore db;
    private String currentUserId;
    private TextView noChatsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_chats);

        // אתחול של Firestore ושל המשתמש המחובר
        db = FirebaseFirestore.getInstance();
        currentUserId = "user123"; // פה אתה יכול לשים את מזהה המשתמש המחובר (באמצעות Firebase Auth)

        // אתחול RecyclerView
        chatListRecyclerView = findViewById(R.id.chatListRecyclerView);
        noChatsText = findViewById(R.id.noChatsText);
        chatList = new ArrayList<>();
        chatListAdapter = new MyChatsAdapter(chatList);

        chatListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatListRecyclerView.setAdapter(chatListAdapter);

        // טעינת הצ'אטים של המשתמש
        loadUserChats();

        // מאזין ללחיצות על צ'אט
        chatListAdapter.setOnChatClickListener(chatId -> {
            // מעבר לאקטיביטי של הצ'אט עם chatId
            Intent intent = new Intent(MyChatsActivity.this, ChatActivity.class);
            intent.putExtra("chatId", chatId);
            intent.putExtra("currentUserId", currentUserId); // העברת מזהה המשתמש המחובר
            startActivity(intent);
        });
    }

    // פונקציה לטעינת הצ'אטים של המשתמש
    private void loadUserChats() {
        db.collection("chats")
                .whereArrayContains("participants", currentUserId)  // מצא את כל הצ'אטים שהמשתמש משתתף בהם
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String chatId = document.getId();
                            List<String> participants = (List<String>) document.get("participants");
                            String lastUpdated = document.getString("lastUpdated");

                            // יצירת אובייקט Chat לכל צ'אט
                            Chat chat = new Chat(chatId, participants, lastUpdated);
                            chatList.add(chat);
                        }

                        // עדכון ה-RecyclerView
                        if (chatList.isEmpty()) {
                            // אם אין צ'אטים, נציג את ההודעה "אין צאטים זמינים"
                            noChatsText.setVisibility(View.VISIBLE);
                        } else {
                            // אם יש צ'אטים, נסיר את ההודעה
                            noChatsText.setVisibility(View.GONE);
                            chatListAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Toast.makeText(MyChatsActivity.this, "שגיאה בטעינת הצ'אטים", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
