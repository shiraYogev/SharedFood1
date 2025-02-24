package com.example.sharedfood.activitiesAdmin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sharedfood.admin.ManagePostsActivity;
import com.example.sharedfood.R;

public class AdminActionsActivity extends AppCompatActivity {

    /**
     * Called when the activity is created.
     * Initializes the activity, sets up buttons, and handles button clicks to navigate to other activities.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_actions);

        // Button to manage posts
        Button managePostsButton = findViewById(R.id.managePostsButton);
        managePostsButton.setOnClickListener(v -> {
            // Starts the ManagePostsActivity to manage posts
            Intent intent = new Intent(AdminActionsActivity.this, ManagePostsActivity.class);
            startActivity(intent);
        });

        // Button to view the admin list
        Button adminListButton = findViewById(R.id.adminListButton);
        adminListButton.setOnClickListener(v -> {
            // Starts the AdminListActivity to view the list of admins
            Intent intent = new Intent(AdminActionsActivity.this, AdminListActivity.class);
            startActivity(intent);
        });

        // Button to view the user list
        Button userListButton = findViewById(R.id.userListButton);
        userListButton.setOnClickListener(v -> {
            // Starts the UserListActivity to view the list of users
            Intent intent = new Intent(AdminActionsActivity.this, UserListActivity.class);
            startActivity(intent);
        });
    }
}
