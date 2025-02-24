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

    private FirebaseAuth mAuth;
    private FirebaseFirestore db; // Michael, 27/01/2025 - הוספת משתנה Firebase Firestore
    private EditText emailEditText, passwordEditText, confirmPasswordEditText;
    private Button signUpButton;
    TextView loginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Michael, 27/01/2025 - אתחול Firestore

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        signUpButton = findViewById(R.id.signUpButton);
        loginLink = findViewById(R.id.loginLink);
        ScrollView scroll = findViewById(R.id.termsScrollView);
        TextView textView = findViewById(R.id.termsTextView);
        CheckBox termsCheckBox = findViewById(R.id.termsCheckBox);


        textView.setText(Html.fromHtml(getString(R.string.terms_and_conditions), Html.FROM_HTML_MODE_LEGACY));

        termsCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            signUpButton.setEnabled(isChecked); // הפעלת הכפתור רק אם ה-CheckBox מסומן
        });

        loginLink.setOnClickListener(v -> {
            // ניווט ל-LoginActivity כשיש חשבון קיים
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();  // סיום ה-Activity הנוכחי
        });

        signUpButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(SignUpActivity.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(SignUpActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!termsCheckBox.isChecked()) {
                Toast.makeText(SignUpActivity.this, "אנא קרא/י את התקנון, ואשר/י את התנאים וההגבלות", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {

                                Map<String, Object> userData = new HashMap<>();
                                userData.put("email", email);
                                userData.put("is_banned", false);

                                // שמירה של המידע ב-Firestore
                                db.collection("users")
                                        .document(email) // שימוש ישיר במייל כ-ID
                                        .set(userData)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(SignUpActivity.this, "User added to Firestore", Toast.LENGTH_SHORT).show();
                                            // ניווט למסך הגדרת שם משתמש אחרי הצלחה בשמירה
                                            Intent intent = new Intent(SignUpActivity.this, SetUsernameActivity.class);
                                            startActivity(intent);
                                            finish();  // מסיים את ה-activity הנוכחי
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(SignUpActivity.this, "Failed to add user to Firestore", Toast.LENGTH_SHORT).show();
                                        });
                            }
                            Toast.makeText(SignUpActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SignUpActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
