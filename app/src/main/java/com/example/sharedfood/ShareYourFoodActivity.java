package com.example.sharedfood;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;

public class ShareYourFoodActivity extends AppCompatActivity {

    private EditText foodDescriptionEditText;  // EditText to input the food description
    private Button shareFoodButton, uploadPhotoButton;  // Buttons for sharing food and uploading photos
    private FirebaseFirestore db;  // Firestore instance to interact with Firebase database

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_your_food);  // Set the layout for this activity

        // Initialize the views by finding them by their IDs in the layout
        foodDescriptionEditText = findViewById(R.id.foodDescriptionEditText);
        shareFoodButton = findViewById(R.id.shareFoodButton);
        uploadPhotoButton = findViewById(R.id.uploadPhotoButton);
        db = FirebaseFirestore.getInstance();  // Initialize Firestore instance

        // Set up the "Share Food" button's click listener
        shareFoodButton.setOnClickListener(v -> {
            String foodDescription = foodDescriptionEditText.getText().toString().trim();  // Get the text input for the food description

            if (!foodDescription.isEmpty()) {  // Check if the description is not empty
                // Create a new FoodPost object with the food description
                FoodPost foodPost = new FoodPost(foodDescription);

                // Save the food post to Firestore under the "foodPosts" collection
                db.collection("foodPosts")
                        .add(foodPost)  // Add the food post document to Firestore
                        .addOnSuccessListener(documentReference -> {
                            // If successful, show a success message and finish the activity (return to home screen)
                            Toast.makeText(ShareYourFoodActivity.this, "Food shared successfully!", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            // If failed, show an error message
                            Toast.makeText(ShareYourFoodActivity.this, "Failed to share food", Toast.LENGTH_SHORT).show();
                        });
            } else {
                // If the description is empty, show a message asking the user to enter a description
                Toast.makeText(ShareYourFoodActivity.this, "Please enter a description", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up the "Upload Photo" button's click listener (future feature)
        uploadPhotoButton.setOnClickListener(v -> {
            // Display a message for now since the photo upload feature is coming soon
            Toast.makeText(ShareYourFoodActivity.this, "Photo upload feature coming soon!", Toast.LENGTH_SHORT).show();
        });
    }
}
