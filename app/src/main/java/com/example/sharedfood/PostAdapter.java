package com.example.sharedfood;

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

import com.example.sharedfood.post.Post;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private List<Post> posts;
    private PostClickListener listener;
    private Context context;

    public PostAdapter(List<Post> posts, Context context) {
        this.posts = posts;
        this.context = context;
    }

    public interface PostClickListener {
        void onEditClick(Post post);
        void onDeleteClick(Post post);
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
            // Open ChatActivity and pass the userId of the post owner
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("userId", post.getUserId());  // Pass the owner userId
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView descriptionText;
        TextView locationText;
        ChipGroup filtersChipGroup;
        ImageButton chatButton;  // Add chat button

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
