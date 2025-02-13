package com.example.sharedfood;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private EditText emailEditText, passwordEditText;

    Button loginButton;
    TextView registerLink;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText); 
        loginButton = findViewById(R.id.loginButton);
        registerLink = findViewById(R.id.registerLink);

        // ניווט למסך ההרשמה בלחיצה על הלינק
        registerLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(LoginActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }
////////////////////////////////////////////////// Michael START 3/2/2025
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("banned_users").document(email).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Toast.makeText(LoginActivity.this, "החשבון שלך חסום לצמיתות. צור קשר עם התמיכה.", Toast.LENGTH_SHORT).show();
                        } else {
                           
                            db.collection("temp_banned_users").document(email).get()
                                    .addOnSuccessListener(tempDoc -> {
                                        if (tempDoc.exists()) {
                                            Toast.makeText(LoginActivity.this, "החשבון שלך חסום זמנית. צור קשר עם התמיכה.", Toast.LENGTH_SHORT).show();
                                        } else {                                   
                                            mAuth.signInWithEmailAndPassword(email, password)
                                                    .addOnCompleteListener(this, task -> {
                                                        if (task.isSuccessful()) {
                                                            FirebaseUser user = mAuth.getCurrentUser();
                                                            checkIfUserIsBannedOrAdmin(user);
                                                        } else {
                                                            Toast.makeText(LoginActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(LoginActivity.this, "שגיאה בבדיקת סטטוס החשבון. נסה שוב מאוחר יותר.", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(LoginActivity.this, "שגיאה בבדיקת סטטוס החשבון. נסה שוב מאוחר יותר.", Toast.LENGTH_SHORT).show();
                    });

            return;
////////////////////////////////////////////////// END 3/2/2025

        });
    }

    private void checkIfUserIsBannedOrAdmin(FirebaseUser user) {
        if (user == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance().collection("banned_users")
                .document(user.getEmail())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Toast.makeText(this, "Your account is banned. Contact support.", Toast.LENGTH_SHORT).show();
                        FirebaseAuth.getInstance().signOut();
                        finish();
                    } else {
                        checkIfUserIsAdmin(user);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("LoginActivity", "Failed to check ban status", e);
                    Toast.makeText(this, "Error checking ban status. Please try again.", Toast.LENGTH_SHORT).show();
                    FirebaseAuth.getInstance().signOut();
                    finish();
                });
    }
    private void checkIfUserIsAdmin(FirebaseUser user) {
        MainActivity.isAdmin(user, isAdmin -> {
            if (isAdmin) {
              
                Toast.makeText(LoginActivity.this, "ברוך הבא, אדון מנהל! \n בשביל פעולות מנהלים לחץ על \"צור קשר\"", Toast.LENGTH_SHORT).show();
            }

            Intent intent = new Intent(LoginActivity.this, HomePageActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
