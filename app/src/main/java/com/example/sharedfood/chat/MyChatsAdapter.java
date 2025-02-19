package com.example.sharedfood.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sharedfood.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class MyChatsAdapter extends RecyclerView.Adapter<MyChatsAdapter.ChatViewHolder> {
    private List<Chat> chatList;
    protected OnChatClickListener listener;
    protected FirebaseFirestore db;

    // Constructor
    public MyChatsAdapter(List<Chat> chatList, OnChatClickListener listener) {
        this.chatList = chatList;
        this.listener = listener;
        db = FirebaseFirestore.getInstance();
    }

    // ViewHolder for Chat Item
    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText;
        ImageButton deleteChatButton;
        View chatCard;

        public ChatViewHolder(View view) {
            super(view);
            usernameText = view.findViewById(R.id.usernameText);
            deleteChatButton = view.findViewById(R.id.deleteChatButton);  // כפתור מחיקה
            chatCard = view.findViewById(R.id.chatCard);  // כרטיס הצ'אט כולו (ללחיצה)
        }
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = chatList.get(position);

        // קבלת מזהה המשתמש השני
        String otherUserId = chat.getOtherUserId();

        // שליפת שם המשתמש השני
        db.collection("users").document(otherUserId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String username = task.getResult().getString("username");
                        holder.usernameText.setText(username != null ? username : "Unknown User");
                    }
                });

        // הוספת פעולה ללחיצה על כרטיס הצ'אט
        holder.chatCard.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChatClick(chat.getChatId());
            }
        });

        // הוספת פעולה לכפתור המחיקה
        holder.deleteChatButton.setOnClickListener(v -> {
            // טיפול בהסרת הצ'אט מה-Firebase
            db.collection("chats").document(chat.getChatId()).delete()
                    .addOnSuccessListener(aVoid -> {
                        // הצלחה במחיקת הצ'אט
                        chatList.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(v.getContext(), "Chat deleted successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        // שגיאה במחיקת הצ'אט
                        Toast.makeText(v.getContext(), "Failed to delete chat", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public interface OnChatClickListener {
        void onChatClick(String chatId);  // פעולה כאשר לוחצים על שיחה
    }
}
