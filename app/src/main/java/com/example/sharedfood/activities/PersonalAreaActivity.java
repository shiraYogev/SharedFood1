package com.example.sharedfood.activities;

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

public class PersonalAreaActivity extends AppCompatActivity {

    private TextView userDetailsTextView;
    private Button logoutButton, deleteAccountButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_area);

        userDetailsTextView = findViewById(R.id.userDetailsTextView);
        logoutButton = findViewById(R.id.logoutButton);
        deleteAccountButton = findViewById(R.id.deleteAccountButton);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // הצגת פרטי המשתמש
        displayUserDetails();

        // התנתקות
        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(PersonalAreaActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // מחיקת חשבון
        deleteAccountButton.setOnClickListener(v -> deleteAccount());
    }

    private void displayUserDetails() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // הצגת אימייל
            String email = "Email: " + user.getEmail();

            // אחזור כמות הפוסטים מהקולקציה
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