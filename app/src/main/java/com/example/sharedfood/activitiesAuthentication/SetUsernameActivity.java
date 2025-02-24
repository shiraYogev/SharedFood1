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

    private EditText usernameEditText;
    private Button saveUsernameButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_username);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        usernameEditText = findViewById(R.id.usernameEditText);
        saveUsernameButton = findViewById(R.id.saveUsernameButton);

        saveUsernameButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();

            if (TextUtils.isEmpty(username)) {
                Toast.makeText(SetUsernameActivity.this, "Please enter a username", Toast.LENGTH_SHORT).show();
                return;
            }

            String userEmail = mAuth.getCurrentUser().getEmail();
            String userId = mAuth.getCurrentUser().getUid(); // Get the current user's ID

            if (userEmail != null) {
                db.collection("users")
                        .document(userEmail)
                        .update("username", username, "userId", userId) // Add the user ID along with the username
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(SetUsernameActivity.this, "שם המשתמש נשמר בהצלחה", Toast.LENGTH_SHORT).show();
                            finish(); // Go back to the main activity
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(SetUsernameActivity.this, "נכשל בשמירת שם המשתמש", Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }
}
