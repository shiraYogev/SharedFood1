package com.example.sharedfood.chat;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sharedfood.R;
import com.google.firebase.Timestamp;

import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Message> messagesList;

    public MessageAdapter(List<Message> messagesList) {
        this.messagesList = messagesList;
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, timestampText;

        public MessageViewHolder(View view) {
            super(view);
            messageText = view.findViewById(R.id.messageText);
            timestampText = view.findViewById(R.id.timestampText);
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messagesList.get(position);
        holder.messageText.setText(message.getMessageText());

        // הצגת ה-Timestamp כ-String
        Timestamp timestamp = message.getTimestamp(); // השגת ה-Timestamp

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // השגת ה-userId של המשתמש הנוכחי

        if (timestamp != null) {
            String time = timestamp.toDate().toString(); // המרת ה-Timestamp ל-String
            holder.timestampText.setText(time);
        } else {
            holder.timestampText.setText("");  // אם ה-Timestamp לא קיים
        }

        // אם ההודעה שייכת למשתמש הנוכחי
        if (message.getUserId().equals(currentUserId)) {
            // בצד ימין עם רקע ירוק בהיר
            holder.messageText.setBackgroundResource(R.drawable.message_background_right);
            holder.messageText.setGravity(Gravity.END);  // align to right
        } else {
            // בצד שמאל עם רקע ירוק כהה
            holder.messageText.setBackgroundResource(R.drawable.message_background_left);
            holder.messageText.setGravity(Gravity.START);  // align to left
        }
    }


    @Override
    public int getItemCount() {
        return messagesList.size();
    }
}
