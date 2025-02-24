/*
 * File: UserListAdapter.java
 * Package: com.example.sharedfood.user
 *
 * Description:
 * This adapter is used to bind a list of User objects to a RecyclerView.
 * It provides administrative actions for each user such as permanently banning/unbanning,
 * applying a temporary ban, and promoting a user to admin status.
 * The adapter interacts with Firebase Firestore to update user data accordingly.
 * Logging statements are included for debugging purposes.
 */

package com.example.sharedfood.user;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sharedfood.R;
import com.example.sharedfood.admin.Admin;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserViewHolder> {

    // Tag used for logging
    private static final String TAG = "UserListAdapter"; // For debugging purposes

    // List of users to be displayed in the RecyclerView
    private final List<User> userList;

    // Context of the calling component (e.g., an Activity)
    private final Context context;

    /**
     * Constructor for UserListAdapter.
     *
     * @param userList The list of User objects to display.
     * @param context  The context in which the adapter is used.
     */
    public UserListAdapter(List<User> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type to represent an item.
     *
     * @param parent   The parent ViewGroup into which the new view will be added.
     * @param viewType The view type of the new view.
     * @return A new instance of UserViewHolder.
     */
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for an individual user item (item_user.xml)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     *
     * @param holder   The ViewHolder which should be updated.
     * @param position The position of the item within the data set.
     */
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        // Get the current User object based on the position
        User user = userList.get(position);

        // Display the user's email address in the corresponding TextView
        holder.userEmailTextView.setText(user.getEmail());

        // Set up click listeners for the action buttons
        holder.banUserButton.setOnClickListener(v -> handleBanUser(user));
        holder.tempBanUserButton.setOnClickListener(v -> handleTempBanUser(user));
        holder.promoteUserButton.setOnClickListener(v -> handlePromoteUser(user));

        // Log the listener setup for debugging
        Log.d(TAG, "onBindViewHolder: Set listeners for " + user.getEmail());

        // Update the "ban" button state based on whether the user is banned
        if (user.isBanned()) {
            // If banned, set text to "Unban" (in Hebrew: בטל חסימה) and use a green tint
            holder.banUserButton.setText("בטל חסימה");
            holder.banUserButton.setBackgroundTintList(context.getColorStateList(R.color.green_color));
        } else {
            // If not banned, set text to "Ban" (in Hebrew: חסום) and use a red tint
            holder.banUserButton.setText("חסום");
            holder.banUserButton.setBackgroundTintList(context.getColorStateList(R.color.red_color));
        }

        // Update the temporary ban button state based on whether the user is currently temporarily banned
        if (user.getTempBanTime() != null && user.getTempBanTime() > System.currentTimeMillis()) {
            // If currently temp banned, set button text to "Cancel Temporary Ban" (in Hebrew: בטל חסימה זמנית)
            // and use a green tint
            holder.tempBanUserButton.setText("בטל חסימה זמנית");
            holder.tempBanUserButton.setBackgroundTintList(context.getColorStateList(R.color.green_color));
        } else {
            // If not temp banned, set button text to "Temp Ban" (in Hebrew: חסום זמנית)
            // and use a blue tint
            holder.tempBanUserButton.setText("חסום זמנית");
            holder.tempBanUserButton.setBackgroundTintList(context.getColorStateList(R.color.blue_color));
        }
    }

    /**
     * Returns the total number of items in the user list.
     *
     * @return The number of users.
     */
    @Override
    public int getItemCount() {
        return userList.size();
    }

    /**
     * Handles the permanent ban/unban action for a given user.
     *
     * @param user The user to be banned or unbanned.
     */
    private void handleBanUser(User user) {
        Log.d(TAG, "handleBanUser called for user: " + user.getEmail()); // Debug log
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Reference to the user's document in the "users" collection, identified by the user's email
        DocumentReference userRef = db.collection("users").document(user.getEmail());

        if (user.isBanned()) {
            // If the user is already banned, unban them by updating the "is_banned" field to false
            userRef.update("is_banned", false)
                    .addOnSuccessListener(aVoid -> {
                        user.setBanned(false);
                        Toast.makeText(context, "משתמש הוסר מחסימה לצמיתות", Toast.LENGTH_SHORT).show();
                        notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error unbanning user", e);
                        Toast.makeText(context, "שגיאה בהסרת חסימה", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // If the user is not banned, ban them by updating the "is_banned" field to true
            userRef.update("is_banned", true)
                    .addOnSuccessListener(aVoid -> {
                        user.setBanned(true);
                        Toast.makeText(context, "משתמש נחסם לצמיתות", Toast.LENGTH_SHORT).show();
                        notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error banning user", e);
                        Toast.makeText(context, "שגיאה בחסימה", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    /**
     * Handles the temporary ban action for a given user.
     * Opens an AlertDialog for the admin to select a ban duration.
     *
     * @param user The user to be temporarily banned.
     */
    private void handleTempBanUser(User user) {
        Log.d(TAG, "handleTempBanUser called for user: " + user.getEmail()); // Debug log

        // Build an AlertDialog to let the admin choose the temporary ban duration
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("בחר את משך החסימה")
                .setItems(new CharSequence[]{"3 שעות", "יום אחד", "3 ימים", "שבוע", "חודש"}, (dialog, which) -> {
                    long durationInMillis = 0;
                    // Determine the duration based on the selected option
                    switch (which) {
                        case 0: // 3 hours
                            durationInMillis = 3 * 60 * 60 * 1000;
                            break;
                        case 1: // 1 day
                            durationInMillis = 24 * 60 * 60 * 1000;
                            break;
                        case 2: // 3 days
                            durationInMillis = 3 * 24 * 60 * 60 * 1000;
                            break;
                        case 3: // 1 week
                            durationInMillis = 7 * 24 * 60 * 60 * 1000;
                            break;
                        case 4: // 1 month
                            durationInMillis = 30L * 24 * 60 * 60 * 1000;
                            break;
                    }
                    // Calculate the timestamp when the temporary ban will end
                    long tempBanTime = System.currentTimeMillis() + durationInMillis;

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    // Update the user's document with the new temporary ban time
                    db.collection("users").document(user.getEmail())
                            .update("temp_ban_time", tempBanTime)
                            .addOnSuccessListener(aVoid -> {
                                user.setTempBanTime(tempBanTime);
                                Toast.makeText(context, "משתמש נחסם זמנית", Toast.LENGTH_SHORT).show();
                                notifyDataSetChanged();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error applying temp ban", e);
                                Toast.makeText(context, "שגיאה בחסימה זמנית", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("ביטול", null)
                .show();
    }

    /**
     * Handles the promotion of a user to admin.
     *
     * @param user The user to be promoted.
     */
    private void handlePromoteUser(User user) {
        Log.d(TAG, "handlePromoteUser called for user: " + user.getEmail()); // Debug log
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Add the user to the "admins" collection by creating a new Admin object.
        db.collection("admins").document(user.getEmail())
                .set(new Admin(user.getEmail(), false))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "משתמש הועלה למנהל", Toast.LENGTH_SHORT).show();
                    notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error promoting user", e);
                    Toast.makeText(context, "שגיאה בהעלאת משתמש למנהל", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * ViewHolder class that holds the UI elements for a single user item.
     */
    static class UserViewHolder extends RecyclerView.ViewHolder {
        // TextView displaying the user's email address
        TextView userEmailTextView;
        // Buttons for performing actions on the user
        Button banUserButton;
        Button tempBanUserButton;
        Button promoteUserButton;

        /**
         * Constructor for UserViewHolder.
         *
         * @param itemView The view representing a single user item.
         */
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize UI elements using their IDs from the layout (item_user.xml)
            userEmailTextView = itemView.findViewById(R.id.userEmailTextView);
            banUserButton = itemView.findViewById(R.id.banUserButton);
            tempBanUserButton = itemView.findViewById(R.id.tempBanUserButton);
            promoteUserButton = itemView.findViewById(R.id.promoteUserButton);
        }
    }
}
