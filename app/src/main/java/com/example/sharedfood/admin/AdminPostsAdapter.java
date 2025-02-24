package com.example.sharedfood.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sharedfood.R;
import com.example.sharedfood.post.Post;

import java.util.List;

/**
 * Adapter class for displaying a list of posts in the admin panel.
 * Allows the admin to delete or edit posts.
 */
public class AdminPostsAdapter extends RecyclerView.Adapter<AdminPostsAdapter.PostViewHolder> {

    private final List<Post> postList;
    private final OnPostDeleteListener onPostDeleteListener;
    private final OnPostEditListener onPostEditListener;

    /**
     * Interface for handling post deletion actions.
     */
    public interface OnPostDeleteListener {
        void onDeletePost(String postId);
    }

    /**
     * Interface for handling post editing actions.
     */
    public interface OnPostEditListener {
        void onEditPost(Post post);
    }
    // Michael, 8/01/2025, END ########################

    /**
     * Constructor for initializing the adapter with a list of posts and action listeners.
     *
     * @param postList         The list of posts to be displayed.
     * @param deleteListener   Listener for handling post deletion.
     * @param editListener     Listener for handling post editing.
     */
    public AdminPostsAdapter(List<Post> postList, OnPostDeleteListener deleteListener, OnPostEditListener editListener) {
        this.postList = postList;
        this.onPostDeleteListener = deleteListener;
        this.onPostEditListener = editListener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);

        // Set post description
        holder.postDescription.setText(post.getDescription());

        // Handle post deletion
        holder.deletePostButton.setOnClickListener(v -> {
            if (onPostDeleteListener != null) {
                onPostDeleteListener.onDeletePost(post.getId());
            }
        });

        // Handle post editing
        holder.editPostButton.setOnClickListener(v -> {
            if (onPostEditListener != null) {
                onPostEditListener.onEditPost(post);
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    /**
     * ViewHolder class for managing individual post items.
     */
    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView postDescription;
        Button deletePostButton;

        ImageButton editPostButton;

        /**
         * ViewHolder constructor for initializing UI components of a post item.
         *
         * @param itemView The view representing a single post item.
         */
        public PostViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize the TextView that displays the post description
            postDescription = itemView.findViewById(R.id.postDescription);

            // Initialize the delete button for removing a post
            ImageButton deletePostButton = itemView.findViewById(R.id.deletePostButton);
        }

    }
}
