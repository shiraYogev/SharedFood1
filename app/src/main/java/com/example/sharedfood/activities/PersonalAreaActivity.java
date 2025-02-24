package com.example.sharedfood.activities;

// Imports for Android components, Firebase authentication, and Firestore database
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sharedfood.R;
import com.example.sharedfood.activitiesAuthentication.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * Activity class for managing the user's personal area, including displaying user details,
 * logging out, and deleting the account.
 */
public class PersonalAreaActivity extends AppCompatActivity {

    // UI elements and Firebase instances
    private TextView userDetailsTextView;
    private Button logoutButton, deleteAccountButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    /**
     * Initializes the activity, sets up UI components, and configures button listeners.
     * @param savedInstanceState The saved state of the activity, if any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_area);

        // Initialize UI elements
        userDetailsTextView = findViewById(R.id.userDetailsTextView);
        logoutButton = findViewById(R.id.logoutButton);
        deleteAccountButton = findViewById(R.id.deleteAccountButton);

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Display the logged-in user's details
        displayUserDetails();

        // Set up logout button listener
        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(PersonalAreaActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // Set up delete account button listener
        deleteAccountButton.setOnClickListener(v -> deleteAccount());
    }

    /**
     * Retrieves and displays the current user's email and number of posts from Firestore.
     * Updates the UI accordingly.
     */
    private void displayUserDetails() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Display user's email
            String email = "Email: " + user.getEmail();

            // Query Firestore to get the number of posts by the user
            db.collection("foodPosts")
                    .whereEqualTo("userId", user.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            int postCount = querySnapshot != null ? querySnapshot.size() : 0;

                            String postInfo = "Number of posts: " + postCount;
                            userDetailsTextView.setText(email + "\n");
                        } else {
                            userDetailsTextView.setText(email + "\n");
                        }
                    });
        } else {
            userDetailsTextView.setText("No user is logged in.");
        }
    }

    /**
     * Deletes the current user's account from Firebase Authentication.
     * Shows a success or failure message and redirects to the login screen on success.
     */
    private void deleteAccount() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.delete()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Account deleted successfully.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(PersonalAreaActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this, "Failed to delete account: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "No user is logged in.", Toast.LENGTH_SHORT).show();
        }
    }
}