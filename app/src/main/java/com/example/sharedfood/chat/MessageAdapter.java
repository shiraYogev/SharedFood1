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
/**
 * Adapter for displaying chat messages in a RecyclerView.
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Message> messagesList; // List of messages to be displayed

    /**
     * Constructor to initialize the adapter with a list of messages.
     *
     * @param messagesList List of chat messages.
     */
    public MessageAdapter(List<Message> messagesList) {
        this.messagesList = messagesList;
    }

    /**
     * ViewHolder class that represents each chat message item.
     */
    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, timestampText; // Message content and timestamp views

        /**
         * Constructor for the ViewHolder.
         *
         * @param view The layout view for each chat message.
         */
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
        Message message = messagesList.get(position); // Get the current message
        holder.messageText.setText(message.getMessageText()); // Display the message text

        // Retrieve the message timestamp
        Timestamp timestamp = message.getTimestamp();

        // Get the current user's ID to determine message alignment
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Convert and display the timestamp if available
        if (timestamp != null) {
            String time = timestamp.toDate().toString(); // Convert Timestamp to String
            holder.timestampText.setText(time);
        } else {
            holder.timestampText.setText("");  // Display empty if no timestamp is available
        }

        // Align the message based on whether it was sent by the current user
        if (message.getUserId().equals(currentUserId)) {
            // Messages from the current user appear on the right with a light green background
            holder.messageText.setBackgroundResource(R.drawable.message_background_right);
            holder.messageText.setGravity(Gravity.END);  // Align text to the right
        } else {
            // Messages from other users appear on the left with a dark green background
            holder.messageText.setBackgroundResource(R.drawable.message_background_left);
            holder.messageText.setGravity(Gravity.START);  // Align text to the left
        }
    }

    @Override
    public int getItemCount() {
        return messagesList.size(); // Return the number of messages
    }
}