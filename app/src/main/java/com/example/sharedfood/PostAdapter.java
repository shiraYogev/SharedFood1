package com.example.sharedfood;

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

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private List<Post> posts;
    private PostClickListener listener;

    public PostAdapter(List<Post> posts) {
        this.posts = posts;
    }

    public interface PostClickListener {
        void onEditClick(Post post);
        void onDeleteClick(Post post);
    }

    public PostAdapter(List<Post> posts, PostClickListener listener) {
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

        public PostViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.postImage);
            descriptionText = view.findViewById(R.id.postDescription);
            locationText = view.findViewById(R.id.postLocation);
            filtersChipGroup = view.findViewById(R.id.filtersChipGroup);
            editButton = view.findViewById(R.id.editPostButton);
            deletePostButton = view.findViewById(R.id.deletePostButton);
        }
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.post_item, parent, false);
        return new PostViewHolder(view);
    }

    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);

        // הצגת התיאור
        holder.descriptionText.setText(post.getDescription() != null ? post.getDescription() : "No description available");

        // הצגת מיקום
        holder.locationText.setText(post.getCity() != null ? post.getCity() : "Location unavailable");

        // הצגת פילטרים
        holder.filtersChipGroup.removeAllViews();
        if (post.getFilters() != null) {
            for (String filter : post.getFilters()) {
                Chip chip = new Chip(holder.filtersChipGroup.getContext());
                chip.setText(filter);
                chip.setCheckable(false);
                holder.filtersChipGroup.addView(chip);
            }
        }

        // הצגת תמונה
        if (post.getImageBitmap() != null) {
            holder.imageView.setImageBitmap(post.getImageBitmap());
            holder.imageView.setVisibility(View.VISIBLE);
        } else {
            holder.imageView.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return posts.size();
    }
}