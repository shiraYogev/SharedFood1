package com.example.sharedfood.post;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sharedfood.activities.ChatActivity;
import com.example.sharedfood.R;
import com.example.sharedfood.chat.ChatManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;
import java.util.UUID;
import com.google.firebase.auth.FirebaseAuth;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private List<Post> posts; // List of posts to display
    private Context context; // Context for accessing resources and starting activities
    private ChatManager chatManager; // Manager for chat functionalities

    public PostAdapter(List<Post> posts, Context context) {
        this.posts = posts;
        this.context = context;
        chatManager = new ChatManager();  // Initialize ChatManager
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);

        holder.descriptionText.setText(post.getDescription() != null ? post.getDescription() : "No description available");
        holder.locationText.setText(post.getCity() != null ? post.getCity() : "Location unavailable");

        // Add filters (if any)
        holder.filtersChipGroup.removeAllViews();
        if (post.getFilters() != null) {
            for (String filter : post.getFilters()) {
                Chip chip = new Chip(holder.filtersChipGroup.getContext());
                chip.setText(filter);
                chip.setCheckable(false);
                holder.filtersChipGroup.addView(chip);
            }
        }

        // Add image if available
        if (post.getImageBitmap() != null) {
            holder.imageView.setImageBitmap(post.getImageBitmap());
            holder.imageView.setVisibility(View.VISIBLE);
        } else {
            holder.imageView.setVisibility(View.GONE);
        }

        // Set up chat button click
        holder.chatButton.setOnClickListener(v -> {
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get current user ID
            String chatId = UUID.randomUUID().toString(); // Generate a unique chat ID
            String postUserId = post.getUserId(); // Get post user's ID

            // Create a new chat
            chatManager.createChat(chatId, currentUserId, postUserId); // Pass current user ID and post user ID separately

            // Start chat activity with the generated chat ID
            Intent intent = new Intent(holder.itemView.getContext(), ChatActivity.class);
            intent.putExtra("chatId", chatId);
            intent.putExtra("currentUserId", currentUserId);
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return posts.size(); // Return the number of posts
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView descriptionText;
        TextView locationText;
        ChipGroup filtersChipGroup;
        ImageButton chatButton;  // Chat button

        public PostViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.postImage);
            descriptionText = view.findViewById(R.id.postDescription);
            locationText = view.findViewById(R.id.postLocation);
            filtersChipGroup = view.findViewById(R.id.filtersChipGroup);
            chatButton = view.findViewById(R.id.chatButton);  // Initialize chat button
        }
    }
}
