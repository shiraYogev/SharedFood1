package com.example.sharedfood.post;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sharedfood.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class MyPostsAdapter extends RecyclerView.Adapter<MyPostsAdapter.PostViewHolder> {
    private List<Post> posts; // List of posts to display
    private PostDeleteListener deleteListener; // Listener for delete events
    private PostEditListener editListener; // Listener for edit events

    // Interface to handle edit events
    public interface PostEditListener {
        void onEditClick(Post post); // Event triggered when an edit button is clicked
    }

    // Interface to handle delete events
    public interface PostDeleteListener {
        void onDeleteClick(Post post); // Event triggered when a delete button is clicked
    }

    // Constructor that receives a list of posts and listeners for both events
    public MyPostsAdapter(List<Post> posts, PostDeleteListener deleteListener, PostEditListener editListener) {
        this.posts = posts; // Initialize the posts list
        this.deleteListener = deleteListener; // Initialize the delete listener
        this.editListener = editListener; // Initialize the edit listener
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView; // ImageView for displaying post image
        TextView descriptionText; // TextView for displaying post description
        TextView locationText; // TextView for displaying post location
        ChipGroup filtersChipGroup; // ChipGroup for displaying filters
        ImageButton editPostButton; // Button for editing the post
        ImageButton deletePostButton; // Button for deleting the post

        public PostViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.postImage); // Initialize ImageView
            descriptionText = view.findViewById(R.id.postDescription); // Initialize description TextView
            locationText = view.findViewById(R.id.postLocation); // Initialize location TextView
            filtersChipGroup = view.findViewById(R.id.filtersChipGroup); // Initialize ChipGroup for filters
            editPostButton = view.findViewById(R.id.editPostButton); // Initialize edit button
            deletePostButton = view.findViewById(R.id.deletePostButton); // Initialize delete button
        }
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false); // Inflate the item layout for each post
        return new PostViewHolder(view); // Return a new ViewHolder
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position); // Get the post at the current position

        // Set the description or a default message if not available
        holder.descriptionText.setText(post.getDescription() != null ? post.getDescription() : "Description not available");

        // Handle the post image
        if (post.getImageBitmap() != null) {
            holder.imageView.setImageBitmap(post.getImageBitmap()); // Set the image if available
            holder.imageView.setVisibility(View.VISIBLE); // Make the ImageView visible
        } else {
            holder.imageView.setVisibility(View.GONE); // Hide the ImageView if no image is available
        }

        // Set the location or a default message if not available
        holder.locationText.setText(post.getCity() != null ? post.getCity() : "Location not available");

        // Handle filters
        holder.filtersChipGroup.removeAllViews(); // Clear previous filter chips
        if (post.getFilters() != null && !post.getFilters().isEmpty()) {
            for (String filter : post.getFilters()) {
                Chip chip = new Chip(holder.filtersChipGroup.getContext());
                chip.setText(filter); // Set the chip text to the filter
                chip.setCheckable(false); // Make the chip non-checkable
                holder.filtersChipGroup.addView(chip); // Add the chip to the ChipGroup
            }
        } else {
            // If no filters, display a message or avoid adding an empty ChipGroup
            Chip chip = new Chip(holder.filtersChipGroup.getContext());
            chip.setText("No filters"); // Set chip text to indicate no filters
            chip.setCheckable(false); // Make the chip non-checkable
            holder.filtersChipGroup.addView(chip); // Add the chip to the ChipGroup
        }

        // Set click listeners for edit and delete buttons
        holder.editPostButton.setOnClickListener(v -> editListener.onEditClick(post)); // Trigger edit event
        holder.deletePostButton.setOnClickListener(v -> deleteListener.onDeleteClick(post)); // Trigger delete event
    }

    @Override
    public int getItemCount() {
        return posts.size(); // Return the total number of posts
    }
}
