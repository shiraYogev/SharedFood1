/*
 * File: SetUsernameActivity.java
 * Package: com.example.sharedfood.activitiesAuthentication
 *
 * Description:
 * This activity allows a user to set or update their username.
 * It retrieves the username entered by the user, validates that it is not empty,
 * and then updates the corresponding document in the "users" collection in Firestore.
 * The update includes both the username and the user's ID.
 *
 * Upon a successful update, the activity displays a success message and finishes,
 * returning to the previous screen. If the update fails, an error message is shown.
 */

package com.example.sharedfood.activitiesAuthentication;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sharedfood.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SetUsernameActivity extends AppCompatActivity {

    // UI elements
    private EditText usernameEditText;
    private Button saveUsernameButton;

    // Firebase instances
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the layout for this activity
        setContentView(R.layout.activity_set_username);

        // Initialize Firebase Firestore and Auth instances
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Find UI elements by their IDs
        usernameEditText = findViewById(R.id.usernameEditText);
        saveUsernameButton = findViewById(R.id.saveUsernameButton);

        // Set up click listener for the "Save Username" button
        saveUsernameButton.setOnClickListener(v -> {
            // Retrieve the username entered by the user and trim any extra whitespace
            String username = usernameEditText.getText().toString().trim();

            // Validate that the username field is not empty
            if (TextUtils.isEmpty(username)) {
                Toast.makeText(SetUsernameActivity.this, "Please enter a username", Toast.LENGTH_SHORT).show();
                return;
            }

            // Retrieve the current user's email and UID
            String userEmail = mAuth.getCurrentUser().getEmail();
            String userId = mAuth.getCurrentUser().getUid(); // Get the current user's ID

            // If the user's email is available, update the Firestore document
            if (userEmail != null) {
                db.collection("users")
                        .document(userEmail)
                        // Update the document with the new username and user ID
                        .update("username", username, "userId", userId)
                        .addOnSuccessListener(aVoid -> {
                            // Inform the user of successful update and finish the activity
                            Toast.makeText(SetUsernameActivity.this, "שם המשתמש נשמר בהצלחה", Toast.LENGTH_SHORT).show();
                            finish(); // Go back to the main activity
                        })
                        .addOnFailureListener(e -> {
                            // Inform the user if saving the username failed
                            Toast.makeText(SetUsernameActivity.this, "נכשל בשמירת שם המשתמש", Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }
}
