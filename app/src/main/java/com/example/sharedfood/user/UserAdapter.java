/*
 * File: UserAdapter.java
 * Package: com.example.sharedfood.user
 *
 * Description:
 * This adapter is used to bind a list of User objects to a RecyclerView.
 * It displays each user's email and provides action buttons to ban, temporarily ban, or promote the user.
 * The adapter uses a custom ViewHolder (UserViewHolder) to reference and update the UI elements for each list item.
 *
 * The UserActionListener interface is used to notify when an action button is clicked,
 * passing the corresponding User object and a string representing the action ("ban", "temp_ban", or "promote").
 */

package com.example.sharedfood.user;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sharedfood.R;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    // List holding the User objects to be displayed
    private final List<User> userList;

    // Listener to handle user actions (ban, temp ban, promote)
    private final UserActionListener listener;

    /**
     * Interface for handling actions on a User.
     * The onAction method is called with the User object and an action identifier.
     */
    public interface UserActionListener {
        void onAction(User user, String action);
    }

    /**
     * Constructor for UserAdapter.
     *
     * @param userList The list of User objects to display.
     * @param listener The listener for user action events.
     */
    public UserAdapter(List<User> userList, UserActionListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type to represent an item.
     *
     * @param parent   The ViewGroup into which the new View will be added.
     * @param viewType The view type of the new View.
     * @return A new UserViewHolder that holds a View for an individual user item.
     */
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout (item_user.xml) for a user list item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    /**
     * Called by RecyclerView to display data at the specified position.
     *
     * @param holder   The ViewHolder to update.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        // Get the User object at the given position
        User user = userList.get(position);

        // Set the user's email in the TextView
        holder.emailTextView.setText(user.getEmail());

        // Update the ban button text based on the user's ban status.
        // If the user is banned, the button displays "בטל חסימה" (Unban), otherwise it displays "חסום" (Ban).
        holder.banButton.setText(user.isBanned() ? "בטל חסימה" : "חסום");
        // Set click listener for the ban button to trigger the "ban" action
        holder.banButton.setOnClickListener(v -> listener.onAction(user, "ban"));

        // Set click listener for the temporary ban button to trigger the "temp_ban" action
        holder.tempBanButton.setOnClickListener(v -> listener.onAction(user, "temp_ban"));

        // Set click listener for the promote button to trigger the "promote" action
        holder.promoteButton.setOnClickListener(v -> listener.onAction(user, "promote"));
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The size of the userList.
     */
    @Override
    public int getItemCount() {
        return userList.size();
    }

    /**
     * Updates the list of users displayed by the adapter.
     *
     * @param updatedList The new list of User objects.
     */
    public void updateUsers(List<User> updatedList) {
        userList.clear();
        userList.addAll(updatedList);
        notifyDataSetChanged();
    }

    /**
     * The ViewHolder class that represents an individual user item in the RecyclerView.
     * It holds references to the UI elements for a single user (email and action buttons).
     */
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        // TextView for displaying the user's email
        TextView emailTextView;
        // Buttons for user actions: ban/unban, temporary ban, and promote
        Button banButton, tempBanButton, promoteButton;

        /**
         * Constructor for the ViewHolder.
         *
         * @param itemView The view representing a single user item.
         */
        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize the UI elements from the item layout
            emailTextView = itemView.findViewById(R.id.userEmailTextView);
            banButton = itemView.findViewById(R.id.banUserButton);
            tempBanButton = itemView.findViewById(R.id.tempBanUserButton);
            promoteButton = itemView.findViewById(R.id.promoteUserButton);
        }
    }
}
