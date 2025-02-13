package com.example.sharedfood.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sharedfood.R;
import java.util.List;

public class MyChatsAdapter extends RecyclerView.Adapter<MyChatsAdapter.ChatViewHolder> {

    private List<Chat> chatList;
    private OnChatClickListener onChatClickListener;

    // קונסטרוקטור
    public MyChatsAdapter(List<Chat> chatList) {
        this.chatList = chatList;
    }

    // ממשק לביצוע פעולה כשנלחץ על צ'אט
    public interface OnChatClickListener {
        void onChatClick(String chatId);
    }

    // הגדרת מאזין ללחיצות על צ'אטים
    public void setOnChatClickListener(OnChatClickListener listener) {
        this.onChatClickListener = listener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // יצירת פריט חדש ברשימה (פריט שיחה)
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat, parent, false); // קובץ ה-XML לכל שיחה
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        // קישור בין המידע של השיחה לתצוגה
        Chat chat = chatList.get(position);
        holder.chatIdText.setText(chat.getChatId());  // הצגת מזהה השיחה (או כל מידע אחר)
        holder.lastUpdatedText.setText(chat.getLastUpdated()); // הצגת זמן העדכון האחרון

        // לחיצה על פריט ברשימה (צ'אט)
        holder.itemView.setOnClickListener(v -> {
            if (onChatClickListener != null) {
                onChatClickListener.onChatClick(chat.getChatId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size(); // מספר הצ'אטים ברשימה
    }

    // מחלקת ViewHolder המתארת את התצוגה של כל שיחה
    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView chatIdText, lastUpdatedText;

        public ChatViewHolder(View view) {
            super(view);
            chatIdText = view.findViewById(R.id.chatIdText);  // טקסט המזהה של השיחה
            lastUpdatedText = view.findViewById(R.id.lastUpdatedText);  // טקסט זמן העדכון האחרון
        }
    }
}
