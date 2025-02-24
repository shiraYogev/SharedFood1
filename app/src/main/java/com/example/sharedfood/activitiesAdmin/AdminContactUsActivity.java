package com.example.sharedfood.activitiesAdmin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sharedfood.R;

public class AdminContactUsActivity extends AppCompatActivity {

    /**
     * Called when the activity is created.
     * Initializes the activity layout and sets up the button for admin actions.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_contact_us);

        // Initialize the button for navigating to admin actions
        Button adminActionsButton = findViewById(R.id.adminActionsButton);
        adminActionsButton.setOnClickListener(v -> {
            // Navigate to the AdminActionsActivity screen
            Intent intent = new Intent(AdminContactUsActivity.this, AdminActionsActivity.class);
            startActivity(intent);
        });
    }
}
