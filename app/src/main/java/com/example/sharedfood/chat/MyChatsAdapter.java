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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * Adapter for displaying a list of chats in a RecyclerView.
 */
public class MyChatsAdapter extends RecyclerView.Adapter<MyChatsAdapter.ChatViewHolder> {
    private List<Chat> chatList; // List of chat objects
    protected OnChatClickListener listener; // Listener for chat item clicks
    protected FirebaseFirestore db; // Firestore database reference

    /**
     * Constructor to initialize the adapter with a list of chats.
     *
     * @param chatList List of chat objects.
     * @param listener Listener for handling chat item clicks.
     */
    public MyChatsAdapter(List<Chat> chatList, OnChatClickListener listener) {
        this.chatList = chatList;
        this.listener = listener;
        db = FirebaseFirestore.getInstance();
    }

    /**
     * ViewHolder class that represents each chat item in the RecyclerView.
     */
    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText; // Displays the other user's name
        ImageButton deleteChatButton; // Button to delete the chat
        View chatCard; // The entire chat item view (clickable)

        /**
         * Constructor for the ViewHolder.
         *
         * @param view The layout view for each chat item.
         */
        public ChatViewHolder(View view) {
            super(view);
            usernameText = view.findViewById(R.id.usernameText);
            deleteChatButton = view.findViewById(R.id.deleteChatButton);
            chatCard = view.findViewById(R.id.chatCard);
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
        Chat chat = chatList.get(position); // Retrieve the chat at the given position

        // Retrieve the ID of the other user in the chat
        String otherUserId = chat.getOtherUserId();

        // Fetch the username of the other user from Firestore
        db.collection("users")
                .whereEqualTo("userId", otherUserId) // Searching by userId
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Retrieve the first matching document
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String username = document.getString("username");
                        holder.usernameText.setText(username != null ? username : "Unknown User");
                    } else {
                        holder.usernameText.setText("User not found");
                    }
                });

        // Handle clicking on a chat item
        holder.chatCard.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChatClick(chat.getChatId());
            }
        });

        // Handle chat deletion when clicking the delete button
        holder.deleteChatButton.setOnClickListener(v -> {
            // Remove the chat from Firestore
            db.collection("chats").document(chat.getChatId()).delete()
                    .addOnSuccessListener(aVoid -> {
                        // Successfully deleted chat
                        chatList.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(v.getContext(), "Chat deleted successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        // Failed to delete chat
                        Toast.makeText(v.getContext(), "Failed to delete chat", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size(); // Return the number of chat items
    }

    /**
     * Interface to handle chat item click events.
     */
    public interface OnChatClickListener {
        void onChatClick(String chatId); // Called when a chat is clicked
    }
}
