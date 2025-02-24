package com.example.sharedfood.activitiesAdmin;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sharedfood.R;
import com.example.sharedfood.user.UserAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.sharedfood.user.User;

/**
 * Activity for managing the list of users.
 * Allows administrators to view users, ban/unban them, apply temporary bans, and promote them to admins.
 */
public class UserListActivity extends AppCompatActivity {
    private static final String TAG = "UserListActivity";
    private RecyclerView userRecyclerView;
    private UserAdapter userAdapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        // Initialize Firebase services
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Set up RecyclerView for displaying users
        userRecyclerView = findViewById(R.id.userRecyclerView);
        userRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(new ArrayList<>(), this::performActionOnUser);
        userRecyclerView.setAdapter(userAdapter);

        // Load user list from Firestore
        loadUsers();
    }

    private void loadUsers() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Retrieve the list of admin users
        db.collection("admins").get().addOnCompleteListener(adminsTask -> {
            if (adminsTask.isSuccessful()) {
                List<String> adminEmails = new ArrayList<>();
                adminsTask.getResult().forEach(admin -> adminEmails.add(admin.getId()));

                // Retrieve the list of regular users (excluding admins)
                db.collection("users").get().addOnCompleteListener(usersTask -> {
                    if (usersTask.isSuccessful()) {
                        List<User> userList = new ArrayList<>();
                        usersTask.getResult().forEach(document -> {
                            String email = document.getId();
                            if (!adminEmails.contains(email)) { // Only add non-admin users
                                boolean isBanned = document.getBoolean("is_banned") != null && document.getBoolean("is_banned");
                                Long tempBanTime = document.contains("temp_ban_time") ? document.getLong("temp_ban_time") : null;
                                userList.add(new User(email, isBanned, tempBanTime));
                            }
                        });
                        userAdapter.updateUsers(userList); // Update the user list in the UI
                    } else {
                        Log.e(TAG, "Failed to load users", usersTask.getException());
                    }
                });
            } else {
                Log.e(TAG, "Failed to load admins", adminsTask.getException());
            }
        });
    }

    /**
     * Handles different actions on a user, such as banning, temporary banning, or promoting to admin.
     */
    private void performActionOnUser(User user, String action) {
        switch (action) {
            case "ban":
                banUser(user);
                break;
            case "temp_ban":
                showTempBanDialog(user); // Show dialog for selecting temporary ban duration
                break;
            case "promote":
                promoteToAdmin(user);
                break;
        }
    }

    /**
     * Displays a dialog to allow the admin to choose a temporary ban duration.
     */
    private void showTempBanDialog(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("בחר את משך החסימה")
                .setItems(new CharSequence[]{"3 שעות", "יום אחד", "3 ימים", "שבוע", "חודש"}, (dialog, which) -> {
                    long durationInHours = 0;
                    // Convert selected option to the corresponding duration in hours
                    switch (which) {
                        case 0: durationInHours = 3; break;   // 3 hours
                        case 1: durationInHours = 24; break;  // 1 day
                        case 2: durationInHours = 72; break;  // 3 days
                        case 3: durationInHours = 168; break; // 1 week
                        case 4: durationInHours = 720; break; // 1 month
                    }

                    tempBanUser(user, durationInHours);
                })
                .setNegativeButton("ביטול", null)
                .show();
    }

    /**
     * Bans or unbans a user.
     */
    private void banUser(User user) {
        boolean isBanned = user.isBanned(); // Check if the user is currently banned
        db.collection("users").document(user.getEmail())
                .update("is_banned", !isBanned) // Toggle the ban status
                .addOnSuccessListener(aVoid -> {
                    if (isBanned) {
                        // If the user was banned, remove them from the banned list
                        db.collection("banned_users").document(user.getEmail())
                                .delete()
                                .addOnSuccessListener(aVoid1 -> {
                                    Toast.makeText(this, "החסימה בוטלה בהצלחה", Toast.LENGTH_SHORT).show();
                                    loadUsers(); // Refresh the user list
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error removing user from banned_users", e);
                                    Toast.makeText(this, "שגיאה בהסרת המשתמש מהאוסף banned_users", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // If the user was not banned, add them to the banned list
                        Map<String, Object> bannedData = new HashMap<>();
                        bannedData.put("email", user.getEmail());
                        bannedData.put("banned_at", System.currentTimeMillis());

                        db.collection("banned_users").document(user.getEmail())
                                .set(bannedData)
                                .addOnSuccessListener(aVoid1 -> {
                                    Toast.makeText(this, "המשתמש נחסם בהצלחה", Toast.LENGTH_SHORT).show();
                                    loadUsers(); // Refresh the user list
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error adding user to banned_users", e);
                                    Toast.makeText(this, "שגיאה בהוספת המשתמש לאוסף banned_users", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating user ban status", e);
                    Toast.makeText(this, "שגיאה בעדכון הסטטוס של המשתמש", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Applies a temporary ban on a user for the selected duration.
     */
    private void tempBanUser(User user, long durationInHours) {
        long currentTimeMillis = System.currentTimeMillis();
        long banEndTimeMillis = currentTimeMillis + (durationInHours * 60 * 60 * 1000);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> tempBanData = new HashMap<>();
        tempBanData.put("email", user.getEmail());
        tempBanData.put("ban_end_time", banEndTimeMillis);

        db.collection("temp_banned_users")
                .document(user.getEmail())
                .set(tempBanData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "משתמש נחסם זמנית", Toast.LENGTH_SHORT).show();
                    loadUsers();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error applying temporary ban", e);
                    Toast.makeText(this, "שגיאה בחסימה זמנית", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Checks and removes expired temporary bans.
     */
    private void checkAndRemoveExpiredTempBans() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        long currentTimeMillis = System.currentTimeMillis();

        db.collection("temp_banned_users")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    querySnapshot.forEach(document -> {
                        Long banEndTime = document.getLong("ban_end_time");
                        if (banEndTime != null && banEndTime < currentTimeMillis) {
                            db.collection("temp_banned_users").document(document.getId()).delete()
                                    .addOnSuccessListener(aVoid -> Log.d(TAG, "חסימה זמנית הסתיימה עבור: " + document.getId()))
                                    .addOnFailureListener(e -> Log.e(TAG, "שגיאה בהסרת חסימה זמנית עבור: " + document.getId(), e));
                        }
                    });
                })
                .addOnFailureListener(e -> Log.e(TAG, "שגיאה בגישה לאוסף temp_banned_users", e));
    }

    /**
     * Promotes a user to admin status.
     */
    private void promoteToAdmin(User user) {
        Map<String, Object> adminData = new HashMap<>();
        adminData.put("email", user.getEmail());
        adminData.put("isSuperAdmin", false);

        db.collection("admins").document(user.getEmail())
                .set(adminData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "המשתמש הועלה לדרגת מנהל", Toast.LENGTH_SHORT).show();
                    loadUsers();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "שגיאה בהפיכת המשתמש למנהל", Toast.LENGTH_SHORT).show();
                });
    }
}
