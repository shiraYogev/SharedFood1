/*
 * File: LoginActivity.java
 * Package: com.example.sharedfood.activitiesAuthentication
 *
 * Description:
 * This activity handles user login for the SharedFood application. It authenticates the user using Firebase,
 * performs additional checks to see if the user is permanently or temporarily banned, and verifies if the user is an admin.
 * If the user passes these checks, they are navigated to the HomePageActivity.
 *
 * Created on: (Date as applicable)
 */

package com.example.sharedfood.activitiesAuthentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sharedfood.activities.HomePageActivity;
import com.example.sharedfood.activities.MainActivity;
import com.example.sharedfood.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    // Firebase authentication instance
    private FirebaseAuth mAuth;

    // UI elements for user input
    private EditText emailEditText, passwordEditText;

    // Buttons and links for login and navigation to registration
    Button loginButton;
    TextView registerLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the layout for this activity
        setContentView(R.layout.activity_login);

        // Initialize Firebase authentication instance
        mAuth = FirebaseAuth.getInstance();

        // Find UI elements by their IDs
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerLink = findViewById(R.id.registerLink);

        // Set up click listener for the registration link to navigate to SignUpActivity
        registerLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        // Set up click listener for the login button
        loginButton.setOnClickListener(v -> {
            // Retrieve and trim email and password from input fields
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // Validate that both email and password are provided
            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(LoginActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            ////////////////////////////////////////////////// Michael START 3/2/2025
            // Create an instance of Firestore to check the user's ban status
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Check if the user is permanently banned by looking for their email in the "banned_users" collection
            db.collection("banned_users").document(email).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Inform the user that their account is permanently banned
                            Toast.makeText(LoginActivity.this, "החשבון שלך חסום לצמיתות. צור קשר עם התמיכה.", Toast.LENGTH_SHORT).show();
                        } else {
                            // If not permanently banned, check if the user is temporarily banned
                            db.collection("temp_banned_users").document(email).get()
                                    .addOnSuccessListener(tempDoc -> {
                                        if (tempDoc.exists()) {
                                            // Inform the user that their account is temporarily banned
                                            Toast.makeText(LoginActivity.this, "החשבון שלך חסום זמנית. צור קשר עם התמיכה.", Toast.LENGTH_SHORT).show();
                                        } else {
                                            // If the user is not banned, attempt to sign in with email and password
                                            mAuth.signInWithEmailAndPassword(email, password)
                                                    .addOnCompleteListener(this, task -> {
                                                        if (task.isSuccessful()) {
                                                            // Retrieve the authenticated user and perform further checks
                                                            FirebaseUser user = mAuth.getCurrentUser();
                                                            checkIfUserIsBannedOrAdmin(user);
                                                        } else {
                                                            // Display an error message if sign in fails
                                                            Toast.makeText(LoginActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        // Error handling for temporary ban status check
                                        Toast.makeText(LoginActivity.this, "שגיאה בבדיקת סטטוס החשבון. נסה שוב מאוחר יותר.", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Error handling for permanent ban status check
                        Toast.makeText(LoginActivity.this, "שגיאה בבדיקת סטטוס החשבון. נסה שוב מאוחר יותר.", Toast.LENGTH_SHORT).show();
                    });

            // Return to exit the click listener after processing the ban checks
            return;
            ////////////////////////////////////////////////// END 3/2/2025

        });
    }

    /**
     * Checks whether the given FirebaseUser is banned.
     * If the user is banned, a message is displayed, the user is signed out, and the activity is finished.
     * Otherwise, it calls checkIfUserIsAdmin to continue the login flow.
     *
     * @param user The FirebaseUser to check.
     */
    private void checkIfUserIsBannedOrAdmin(FirebaseUser user) {
        if (user == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check the "banned_users" collection for the user's email
        FirebaseFirestore.getInstance().collection("banned_users")
                .document(user.getEmail())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // If the user is banned, show a message and sign out
                        Toast.makeText(this, "Your account is banned. Contact support.", Toast.LENGTH_SHORT).show();
                        FirebaseAuth.getInstance().signOut();
                        finish();
                    } else {
                        // If not banned, proceed to check if the user is an admin
                        checkIfUserIsAdmin(user);
                    }
                })
                .addOnFailureListener(e -> {
                    // Log and display error if ban status check fails
                    Log.e("LoginActivity", "Failed to check ban status", e);
                    Toast.makeText(this, "Error checking ban status. Please try again.", Toast.LENGTH_SHORT).show();
                    FirebaseAuth.getInstance().signOut();
                    finish();
                });
    }

    /**
     * Checks whether the authenticated user is an admin.
     * Uses the MainActivity.isAdmin method to perform the check.
     * If the user is an admin, a welcome message is shown.
     * Finally, navigates the user to the HomePageActivity and finishes the current activity.
     *
     * @param user The FirebaseUser to check for admin privileges.
     */
    private void checkIfUserIsAdmin(FirebaseUser user) {
        MainActivity.isAdmin(user, isAdmin -> {
            if (isAdmin) {
                // Display an admin-specific welcome message
                Toast.makeText(LoginActivity.this, "ברוך הבא, אדון מנהל! \n בשביל פעולות מנהלים לחץ על \"צור קשר\"", Toast.LENGTH_SHORT).show();
            }

            // Navigate to the HomePageActivity regardless of admin status
            Intent intent = new Intent(LoginActivity.this, HomePageActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
