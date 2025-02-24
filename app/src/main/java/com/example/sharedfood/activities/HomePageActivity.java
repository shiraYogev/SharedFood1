package com.example.sharedfood.activities;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.content.pm.PackageManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.sharedfood.R;
import com.example.sharedfood.activitiesAdmin.AdminContactUsActivity;
import com.example.sharedfood.activitiesAuthentication.LoginActivity;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;

public class HomePageActivity extends AppCompatActivity {
    private FirebaseAuth mAuth; // Firebase authentication instance
    private FirebaseFirestore db; // Firestore database instance
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1; // Permission request code for notifications

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        // Initialize Firebase authentication and database
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Check and delete expired posts
        checkAndDeleteExpiredPosts();

        // Button to share food
        findViewById(R.id.shareFoodButton).setOnClickListener(v -> {
            if (checkUserLogin()) {
                startActivity(new Intent(HomePageActivity.this, ShareYourFoodActivity.class));
            }
        });

        // Contact us button (redirects based on admin status)
        findViewById(R.id.contactUsButton).setOnClickListener(v -> {
            FirebaseUser user = mAuth.getCurrentUser();
            MainActivity.isAdmin(user, isAdmin -> {
                if (isAdmin) {
                    startActivity(new Intent(HomePageActivity.this, AdminContactUsActivity.class));
                } else {
                    startActivity(new Intent(HomePageActivity.this, activity_contact_us.class));
                }
            });
        });

        // FAQ button
        findViewById(R.id.faqButton).setOnClickListener(v -> {
            startActivity(new Intent(HomePageActivity.this, FAQActivity.class));
        });

        // Personal area button
        findViewById(R.id.personalAreaButton).setOnClickListener(v -> {
            if (checkUserLogin()) {
                startActivity(new Intent(HomePageActivity.this, PersonalAreaActivity.class));
            }
        });

        // Chat button
        findViewById(R.id.chatButton).setOnClickListener(v -> {
            if (checkUserLogin()) {
                startActivity(new Intent(HomePageActivity.this, MyChatsActivity.class));
            }
        });

        // Feed button
        findViewById(R.id.feedButton).setOnClickListener(v -> {
            startActivity(new Intent(HomePageActivity.this, FeedActivity.class));
        });

        // My posts button
        findViewById(R.id.myPostsButton).setOnClickListener(v -> {
            if (checkUserLogin()) {
                startActivity(new Intent(HomePageActivity.this, MyPostsActivity.class));
            }
        });

        // Floating action button (FAB) for sharing food
        ExtendedFloatingActionButton fabShare = findViewById(R.id.fabShare);
        fabShare.setOnClickListener(v -> {
            if (checkUserLogin()) {
                startActivity(new Intent(HomePageActivity.this, ShareYourFoodActivity.class));
            }
        });
    }

    /**
     * Checks for expired posts and deletes them from Firestore.
     * If a post is about to be deleted, a notification is sent.
     */
    private void checkAndDeleteExpiredPosts() {
        db.collection("posts").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Long timestamp = document.getLong("timestamp"); // Retrieve the post creation time
                    if (timestamp != null) {
                        // Calculate expiration time based on filters
                        Long expirationTime = calculateExpirationTime(document);
                        if (System.currentTimeMillis() > expirationTime) {
                            // Send notification before deletion
                            sendPostExpiredNotification(document);

                            // Delete expired post from Firestore
                            db.collection("posts").document(document.getId())
                                    .delete()
                                    .addOnSuccessListener(aVoid -> Log.d("Firebase", "Post deleted: " + document.getId()))
                                    .addOnFailureListener(e -> Log.e("Firebase", "Error deleting post", e));
                        }
                    }
                }
            } else {
                Log.e("Firebase", "Error getting posts: ", task.getException());
            }
        });
    }

    /**
     * Checks whether notification permission is granted.
     *
     * @return True if permission is granted, otherwise false.
     */
    private boolean checkNotificationPermission() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Requests notification permission if it is not already granted.
     */
    private void requestNotificationPermission() {
        if (!checkNotificationPermission()) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                    NOTIFICATION_PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * Sends a notification when a post is about to be deleted.
     *
     * @param document The Firestore document representing the post.
     */
    private void sendPostExpiredNotification(QueryDocumentSnapshot document) {
        if (checkNotificationPermission()) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "expiredPostsChannel")
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("הפוסט שלך עומד להימחק") // Message remains in Hebrew
                    .setContentText("הפוסט '" + document.getString("description") + "' פג תוקף ויימחק בקרוב.")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(document.getId().hashCode(), builder.build());
        } else {
            requestNotificationPermission();
        }
    }

    /**
     * Calculates the expiration time of a post based on its filters.
     *
     * @param document The Firestore document representing the post.
     * @return The expiration timestamp in milliseconds.
     */
    public Long calculateExpirationTime(QueryDocumentSnapshot document) {
        Long timestamp = document.getLong("timestamp"); // Retrieve the post creation time
        if (timestamp == null) return Long.MAX_VALUE; // If no timestamp, do not delete the post

        List<String> filters = (List<String>) document.get("filters"); // Retrieve post filters
        long expirationMillis = 0;

        // Determine expiration time based on filters
        if (filters != null && !filters.isEmpty()) {
            if (filters.contains("Hot")) expirationMillis = Math.max(expirationMillis, 12 * 60 * 60 * 1000); // 12 hours
            if (filters.contains("Cold")) expirationMillis = Math.max(expirationMillis, 72 * 60 * 60 * 1000); // 72 hours (3 days)
            if (filters.contains("Closed")) expirationMillis = Math.max(expirationMillis, 10 * 24 * 60 * 60 * 1000); // 10 days
            if (filters.contains("Dairy")) expirationMillis = Math.max(expirationMillis, 48 * 60 * 60 * 1000); // 48 hours (2 days)
            if (filters.contains("Meat")) expirationMillis = Math.max(expirationMillis, 24 * 60 * 60 * 1000); // 24 hours (1 day)
            if (filters.contains("Vegetables")) expirationMillis = Math.max(expirationMillis, 48 * 60 * 60 * 1000); // 48 hours (2 days)
            if (filters.contains("Pastries")) expirationMillis = Math.max(expirationMillis, 72 * 60 * 60 * 1000); // 72 hours (3 days)
            if (filters.contains("Frizer")) expirationMillis = Math.max(expirationMillis, 30 * 24 * 60 * 60 * 1000); // 30 days
        }


        // If no filters are set, the post does not expire
        if (expirationMillis == 0) {
            return Long.MAX_VALUE;
        }
        return timestamp + expirationMillis;
    }

    /**
     * Checks if the user is logged in.
     * If the user is not logged in, redirects them to the login screen.
     *
     * @return True if the user is logged in, otherwise false.
     */
    private boolean checkUserLogin() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "נא להתחבר תחילה", Toast.LENGTH_SHORT).show(); // Message remains in Hebrew
            startActivity(new Intent(HomePageActivity.this, LoginActivity.class));
            return false;
        }
        return true;
    }
}
