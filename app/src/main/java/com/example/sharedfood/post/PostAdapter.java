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

/**
 * Adapter for displaying posts in a RecyclerView.
 * Each post includes a description, location, filters, an image (if available), and a chat button.
 */
public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private List<Post> posts; // List of posts to display
    private Context context; // Context for accessing resources and starting activities
    private ChatManager chatManager; // Manager for chat functionalities

    /**
     * Constructor for initializing the adapter with a list of posts.
     * @param posts List of posts to be displayed.
     * @param context The application or activity context.
     */
    public PostAdapter(List<Post> posts, Context context) {
        this.posts = posts;
        this.context = context;
        chatManager = new ChatManager();  // Initialize ChatManager for handling chat operations
    }

    /**
     * Creates a new ViewHolder instance when needed.
     * @param parent The parent ViewGroup into which the new view will be added.
     * @param viewType The view type of the new view.
     * @return A new instance of PostViewHolder.
     */
    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item, parent, false);
        return new PostViewHolder(view);
    }

    /**
     * Binds data to the ViewHolder at a specific position.
     * @param holder The ViewHolder to bind data to.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);

        // Set post description (fallback text if null)
        holder.descriptionText.setText(post.getDescription() != null ? post.getDescription() : "No description available");

        // Set post location (fallback text if null)
        holder.locationText.setText(post.getCity() != null ? post.getCity() : "Location unavailable");

        // Remove all filters before adding new ones
        holder.filtersChipGroup.removeAllViews();
        if (post.getFilters() != null) {
            for (String filter : post.getFilters()) {
                Chip chip = new Chip(holder.filtersChipGroup.getContext());
                chip.setText(filter);
                chip.setCheckable(false); // Disable selection
                holder.filtersChipGroup.addView(chip);
            }
        }

        // Set post image if available
        if (post.getImageBitmap() != null) {
            holder.imageView.setImageBitmap(post.getImageBitmap());
            holder.imageView.setVisibility(View.VISIBLE);
        } else {
            holder.imageView.setVisibility(View.GONE);
        }

        // Set up the chat button click event
        holder.chatButton.setOnClickListener(v -> {
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get the current user ID
            String chatId = UUID.randomUUID().toString(); // Generate a unique chat ID
            String postUserId = post.getUserId(); // Get the post owner's user ID

            // Create a new chat session
            chatManager.createChat(chatId, currentUserId, postUserId);  // Pass current user ID and post user ID separately

            // Start ChatActivity with the generated chat ID and current user ID
            Intent intent = new Intent(holder.itemView.getContext(), ChatActivity.class);
            intent.putExtra("chatId", chatId);
            intent.putExtra("currentUserId", currentUserId);
            holder.itemView.getContext().startActivity(intent);
        });
    }

    /**
     * Returns the number of items in the adapter.
     * @return The total number of posts.
     */
    @Override
    public int getItemCount() {
        return posts.size(); // Return the number of posts
    }

    /**
     * ViewHolder class for holding and managing individual post items in the RecyclerView.
     */
    public static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView; // Image of the post
        TextView descriptionText; // Post description
        TextView locationText; // Post location
        ChipGroup filtersChipGroup; // Group of filter chips
        ImageButton chatButton; // Button to initiate a chat

        /**
         * Constructor for initializing UI components in the ViewHolder.
         * @param view The item view.
         */
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
