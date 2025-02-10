package com.example.sharedfood;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private List<Post> posts;
    private PostClickListener listener;
    private Context context;

    public interface PostClickListener {
        void onEditClick(Post post);
        void onDeleteClick(Post post);
    }

    public PostAdapter(Context context, List<Post> posts, PostClickListener listener) {
        this.context = context;
        this.posts = posts;
        this.listener = listener;
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView descriptionText;
        TextView locationText;
        ChipGroup filtersChipGroup;
        ImageButton editButton;
        ImageButton deletePostButton;
        ImageButton chatButton;

        public PostViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.postImage);
            descriptionText = view.findViewById(R.id.postDescription);
            locationText = view.findViewById(R.id.postLocation);
            filtersChipGroup = view.findViewById(R.id.filtersChipGroup);
            editButton = view.findViewById(R.id.editPostButton);
            deletePostButton = view.findViewById(R.id.deletePostButton);
            chatButton = view.findViewById(R.id.chatButton);
        }
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.post_item, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);

        holder.descriptionText.setText(post.getDescription() != null ? post.getDescription() : "No description available");
        holder.locationText.setText(post.getCity() != null ? post.getCity() : "Location unavailable");

        holder.filtersChipGroup.removeAllViews();
        if (post.getFilters() != null) {
            for (String filter : post.getFilters()) {
                Chip chip = new Chip(holder.filtersChipGroup.getContext());
                chip.setText(filter);
                chip.setCheckable(false);
                holder.filtersChipGroup.addView(chip);
            }
        }

        if (post.getImageBitmap() != null) {
            holder.imageView.setImageBitmap(post.getImageBitmap());
            holder.imageView.setVisibility(View.VISIBLE);
        } else {
            holder.imageView.setVisibility(View.GONE);
        }

        holder.chatButton.setOnClickListener(v -> {
            Log.d("PostAdapter", "Chat button clicked");

            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String postOwnerId = post.getUserId();

            Log.d("PostAdapter", "Current User ID: " + currentUserId);
            Log.d("PostAdapter", "Post Owner ID: " + postOwnerId);

            if (currentUserId != null && postOwnerId != null && !currentUserId.equals(postOwnerId)) {
                Log.d("PostAdapter", "Opening chat...");
                openChat(v.getContext(), currentUserId, postOwnerId);
            } else {
                Log.d("PostAdapter", "Chat not opened (same user or null values)");
            }
        });
    }

    private void openChat(Context context, String user1, String user2) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String chatId = user1.compareTo(user2) < 0 ? user1 + "_" + user2 : user2 + "_" + user1;

        Log.d("PostAdapter", "Opening chat with chatId: " + chatId);

        db.collection("chats").document(chatId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("chatId", chatId);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } else {
                Map<String, Object> chatData = new HashMap<>();
                chatData.put("users", Arrays.asList(user1, user2));
                chatData.put("messages", new ArrayList<>());

                db.collection("chats").document(chatId).set(chatData)
                        .addOnSuccessListener(aVoid -> {
                            Intent intent = new Intent(context, ChatActivity.class);
                            intent.putExtra("chatId", chatId);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        });
            }
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }
}
