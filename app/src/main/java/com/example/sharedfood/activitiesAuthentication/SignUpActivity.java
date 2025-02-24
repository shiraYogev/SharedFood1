/*
 * File: SignUpActivity.java
 * Package: com.example.sharedfood.activitiesAuthentication
 *
 * Description:
 * This activity handles the user registration process for the SharedFood application.
 * It collects the user's email, password, and password confirmation, and validates the input.
 * It also ensures that the user agrees to the terms and conditions before proceeding.
 * Upon successful registration via Firebase Authentication, the user's data is saved in Firestore,
 * and the user is navigated to the SetUsernameActivity to set their username.
 */

package com.example.sharedfood.activitiesAuthentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sharedfood.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    // Firebase authentication and Firestore instances
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // UI elements for registration inputs
    private EditText emailEditText, passwordEditText, confirmPasswordEditText;
    private Button signUpButton;
    TextView loginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the layout for the registration screen
        setContentView(R.layout.activity_signup);

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Find UI elements by their IDs


        emailEditText = findViewById(R.id.emailEditText);  // EditText for user to enter their email address
        passwordEditText = findViewById(R.id.passwordEditText);  // EditText for user to enter their password
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText); // EditText for user to confirm their password (for verification)
        signUpButton = findViewById(R.id.signUpButton); // Button that the user clicks to submit the registration form
        loginLink = findViewById(R.id.loginLink); // TextView acting as a clickable link to navigate to the login screen for existing users
        ScrollView scroll = findViewById(R.id.termsScrollView); // ScrollView containing the terms and conditions text for easy reading
        TextView textView = findViewById(R.id.termsTextView); // TextView displaying the terms and conditions content
        CheckBox termsCheckBox = findViewById(R.id.termsCheckBox); // CheckBox that the user must check to agree with the terms and conditions


        // Set the terms and conditions text using HTML formatting
        textView.setText(Html.fromHtml(getString(R.string.terms_and_conditions), Html.FROM_HTML_MODE_LEGACY));

        // Enable the sign-up button only if the terms checkbox is checked
        termsCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            signUpButton.setEnabled(isChecked);
        });

        // Navigate to LoginActivity when the login link is clicked (for existing users)
        loginLink.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();  // End current activity
        });

        // Set up the sign-up button click listener
        signUpButton.setOnClickListener(v -> {
            // Retrieve and trim user inputs
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();

            // Validate that all fields are filled
            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(SignUpActivity.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check that password and confirmation match
            if (!password.equals(confirmPassword)) {
                Toast.makeText(SignUpActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Ensure the terms checkbox is checked
            if (!termsCheckBox.isChecked()) {
                Toast.makeText(SignUpActivity.this, "אנא קרא/י את התקנון, ואשר/י את התנאים וההגבלות", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create a new user with the provided email and password using Firebase Authentication
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Registration was successful
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Prepare user data to store in Firestore
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("email", email);
                                userData.put("is_banned", false);

                                // Save the user data in the "users" collection, using the email as the document ID
                                db.collection("users")
                                        .document(email)
                                        .set(userData)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(SignUpActivity.this, "User added to Firestore", Toast.LENGTH_SHORT).show();
                                            // Navigate to SetUsernameActivity after successful registration and data storage
                                            Intent intent = new Intent(SignUpActivity.this, SetUsernameActivity.class);
                                            startActivity(intent);
                                            finish();  // End current activity
                                        })
                                        .addOnFailureListener(e -> {
                                            // Inform user if saving data to Firestore fails
                                            Toast.makeText(SignUpActivity.this, "Failed to add user to Firestore", Toast.LENGTH_SHORT).show();
                                        });
                            }
                            Toast.makeText(SignUpActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                        } else {
                            // Inform user if registration fails
                            Toast.makeText(SignUpActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
