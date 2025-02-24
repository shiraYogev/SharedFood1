package com.example.sharedfood.activitiesAdmin;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sharedfood.admin.Admin;
import com.example.sharedfood.admin.AdminAdapter;
import com.example.sharedfood.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for displaying and managing the list of administrators.
 * Allows removing admins except for the super admin.
 */
public class AdminListActivity extends AppCompatActivity {
    private static final String TAG = "AdminListActivity";
    private RecyclerView adminRecyclerView;
    private AdminAdapter adminAdapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    /**
     * Called when the activity is created.
     * Initializes UI components and loads the admin list.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_list);

        // Initialize Firebase services
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Set up RecyclerView for displaying admins
        adminRecyclerView = findViewById(R.id.adminRecyclerView);
        adminRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize adapter with an empty list and set up the remove admin listener
        adminAdapter = new AdminAdapter(new ArrayList<>(), this::removeAdmin);
        adminRecyclerView.setAdapter(adminAdapter);

        // Load the list of admins from Firestore
        loadAdmins();
    }

    /**
     * Loads the list of admins from Firestore and updates the RecyclerView.
     */
    private void loadAdmins() {
        db.collection("admins").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Admin> adminList = new ArrayList<>();
                task.getResult().forEach(document -> {
                    String email = document.getId();
                    boolean isSuperAdmin = email.equals("mici9578@gmail.com"); // Identifies the super admin
                    adminList.add(new Admin(email, isSuperAdmin));
                });
                // Update the adapter with the retrieved admin list
                adminAdapter.updateAdmins(adminList);
            } else {
                Log.e(TAG, "Failed to load admins", task.getException());
                Toast.makeText(this, "שגיאה בטעינת הרשימה", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Removes an admin from Firestore unless they are the super admin.
     *
     * @param email The email of the admin to be removed.
     */
    private void removeAdmin(String email) {
        if (email.equals("mici9578@gmail.com")) {
            Toast.makeText(this, "לא ניתן להסיר את המנהל הראשי", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("admins").document(email)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "המנהל הוסר בהצלחה", Toast.LENGTH_SHORT).show();
                    loadAdmins(); // Refresh the admin list
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error removing admin: " + e.getMessage(), e);
                    Toast.makeText(this, "שגיאה בהסרת המנהל: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
