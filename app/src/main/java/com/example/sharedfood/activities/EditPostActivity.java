package com.example.sharedfood.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.sharedfood.post.Post;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Activity for editing an existing post.
 * Extends ShareYourFoodActivity to reuse post creation logic.
 */
public class EditPostActivity extends ShareYourFoodActivity {

    private Post postToEdit; // The post being edited
    private String documentIdToDelete; // Document ID of the original post

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve the post to be edited from the Intent
        postToEdit = getIntent().getParcelableExtra("POST_TO_EDIT");

        if (postToEdit != null) {
            // Show image selection button in case the user wants to change the image
            selectImageButton.setVisibility(View.VISIBLE);

            // Populate fields with existing post data
            foodDescriptionEditText.setText(postToEdit.getDescription());
            cityEditText.setText(postToEdit.getCity());

            // Display the existing image if available
            if (postToEdit.getImageBase64() != null) {
                displayBase64Image(postToEdit.getImageBase64());
            } else {
                // If the image is not stored in memory, try to load it from Firestore
                loadImageFromFirestore(postToEdit.getUserId(), postToEdit.getDescription());
            }

            // Mark the existing filters
            setExistingFilters(postToEdit.getFilters());

            // Change upload button text to "Update Post"
            uploadPostButton.setText("עדכן פוסט");

            // Find the document ID of the original post for deletion
            findDocumentIdToDelete();
        }

        // Change the upload button's behavior to update the post
        uploadPostButton.setOnClickListener(v -> updateExistingPost());
    }

    /**
     * Loads an image from Firestore using userId and post description.
     */
    private void loadImageFromFirestore(String userId, String description) {
        db.collection("posts")
                .whereEqualTo("userId", userId)
                .whereEqualTo("description", description)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String base64Image = document.getString("imageBase64");
                            if (base64Image != null) {
                                displayBase64Image(base64Image);
                            } else {
                                Log.e("EditPost", "No imageBase64 found in Firestore");
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("EditPost", "Error loading image from Firestore", e));
    }

    /**
     * Converts a Base64 string to a Bitmap and displays it in the ImageView.
     */
    private void displayBase64Image(String base64String) {
        Bitmap bitmap = decodeBase64ToBitmap(base64String);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            Log.e("EditPost", "Failed to decode Base64 image.");
        }
    }

    /**
     * Finds the Firestore document ID of the original post for deletion.
     */
    private void findDocumentIdToDelete() {
        db.collection("posts")
                .whereEqualTo("userId", postToEdit.getUserId())
                .whereEqualTo("description", postToEdit.getDescription())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        QueryDocumentSnapshot document = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                        documentIdToDelete = document.getId();
                    }
                });
    }

    @Override
    protected void uploadPost() {
        if (post == null) post = new Post();

        // Copy user ID from the original post
        post.setUserId(postToEdit.getUserId());

        // Upload the updated post
        super.uploadPost();
    }

    @Override
    protected void savePostToFirestore() {
        super.savePostToFirestore();

        // Delete the original post after saving the updated one
        if (documentIdToDelete != null) {
            deleteOriginalPost();
        }
    }

    /**
     * Deletes the original post from Firestore and Firebase Storage.
     */
    private void deleteOriginalPost() {
        // Delete the image from Firebase Storage
        if (postToEdit.getImageUrl() != null) {
            StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(postToEdit.getImageUrl());
            imageRef.delete().addOnSuccessListener(aVoid ->
                    Log.d("DeletePost", "Original image deleted successfully")
            ).addOnFailureListener(e ->
                    Log.e("DeletePost", "Failed to delete original image", e)
            );
        }

        // Delete the post document from Firestore
        db.collection("posts").document(documentIdToDelete)
                .delete()
                .addOnSuccessListener(aVoid ->
                        Log.d("DeletePost", "Original post deleted successfully")
                )
                .addOnFailureListener(e ->
                        Log.e("DeletePost", "Failed to delete original post", e)
                );
    }

    /**
     * Sets the existing filters in the UI based on the post's filters.
     */
    private void setExistingFilters(java.util.List<String> filters) {
        // Reset all checkboxes
        kosherCheckBox.setChecked(false);
        hotCheckBox.setChecked(false);
        coldCheckBox.setChecked(false);
        closedCheckBox.setChecked(false);
        dairyCheckBox.setChecked(false);
        meatCheckBox.setChecked(false);
        veganCheckBox.setChecked(false);
        vegetarianCheckBox.setChecked(false);
        glutenFreeCheckBox.setChecked(false);
        extraKosherCheckBox.setChecked(false);
        frizerCheckBox.setChecked(false);
        pastriesCheckBox.setChecked(false);
        vegetablesCheckBox.setChecked(false);

        // Set filters based on the existing post
        if (filters != null) {
            for (String filter : filters) {
                switch (filter) {
                    case "Kosher": kosherCheckBox.setChecked(true); break;
                    case "Hot": hotCheckBox.setChecked(true); break;
                    case "Cold": coldCheckBox.setChecked(true); break;
                    case "Closed": closedCheckBox.setChecked(true); break;
                    case "Dairy": dairyCheckBox.setChecked(true); break;
                    case "Meat": meatCheckBox.setChecked(true); break;
                    case "vegan": veganCheckBox.setChecked(true); break;
                    case "vegetarian": vegetarianCheckBox.setChecked(true); break;
                    case "glutenFree": glutenFreeCheckBox.setChecked(true); break;
                    case "extraKosher": extraKosherCheckBox.setChecked(true); break;
                    case "frizer": frizerCheckBox.setChecked(true); break;
                    case "pastries": pastriesCheckBox.setChecked(true); break;
                    case "vegetables&fruits": vegetablesCheckBox.setChecked(true); break;
                }
            }
        }
    }

    /**
     * Updates an existing post in Firestore.
     */
    /**
     * Updates an existing post in Firestore with new user input.
     * This method retrieves the updated values from the input fields, verifies them,
     * and then updates the relevant document in Firestore.
     */
    private void updateExistingPost() {
        // Retrieve updated values from input fields
        String updatedDescription = foodDescriptionEditText.getText().toString().trim();
        String updatedCity = cityEditText.getText().toString().trim();

        // Validate the description field - it cannot be empty
        if (updatedDescription.isEmpty()) {
            Toast.makeText(this, "אנא הזן תיאור", Toast.LENGTH_SHORT).show();
            return; // Stop execution if no description is provided
        }

        // Update the list of selected filters based on the UI checkboxes
        updateSelectedFilters();

        // Search for the post in Firestore by matching the user ID and the original description
        db.collection("posts")
                .whereEqualTo("userId", postToEdit.getUserId())  // Find posts by the same user
                .whereEqualTo("description", postToEdit.getDescription()) // Ensure it's the same post
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Loop through the retrieved documents (should be just one in most cases)
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {

                        // Create a map to hold the updated values
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("description", updatedDescription); // Update description
                        updates.put("city", updatedCity); // Update city
                        updates.put("filters", selectedFilters); // Update selected filters

                        // Apply the updates to Firestore
                        document.getReference().update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    // Successfully updated the post in Firestore
                                    Toast.makeText(this, "הפוסט עודכן בהצלחה", Toast.LENGTH_SHORT).show();
                                    navigateToMyPosts(); // Redirect to the user's posts page
                                })
                                .addOnFailureListener(e ->
                                        // Failed to update post in Firestore
                                        Toast.makeText(this, "עדכון הפוסט נכשל", Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e ->
                        // Failed to retrieve the post from Firestore
                        Toast.makeText(this, "שגיאה בחיפוש הפוסט לעדכון", Toast.LENGTH_SHORT).show());
    }


    /**
     * Navigates back to the user's posts activity.
     */
    private void navigateToMyPosts() {
        Intent intent = new Intent(this, MyPostsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
    /**
     * Decodes a Base64 string into a Bitmap image.
     * This is used to convert an image stored as a Base64 string back into an image format.
     *
     * @param base64String The Base64-encoded string representing an image.
     * @return The decoded Bitmap image, or null if decoding fails.
     */
    private Bitmap decodeBase64ToBitmap(String base64String) {
        byte[] decodedBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    /**
     * Updates the list of selected filters based on the checkboxes in the UI.
     * This function ensures that only the selected filters are included in the updated post.
     */
    public void updateSelectedFilters() {
        selectedFilters = new ArrayList<>(); // Ensure the list is initialized

        if (meatCheckBox.isChecked()) selectedFilters.add("Meat");
        if (dairyCheckBox.isChecked()) selectedFilters.add("Dairy");
        if (hotCheckBox.isChecked()) selectedFilters.add("Hot");
        if (coldCheckBox.isChecked()) selectedFilters.add("Cold");
        if (kosherCheckBox.isChecked()) selectedFilters.add("Kosher");
        if (veganCheckBox.isChecked()) selectedFilters.add("vegan");
        if (vegetarianCheckBox.isChecked()) selectedFilters.add("vegetarian");
        if (glutenFreeCheckBox.isChecked()) selectedFilters.add("glutenFree");
        if (extraKosherCheckBox.isChecked()) selectedFilters.add("extraKosher");
        if (frizerCheckBox.isChecked()) selectedFilters.add("frizer");
        if (pastriesCheckBox.isChecked()) selectedFilters.add("pastries");
        if (vegetablesCheckBox.isChecked()) selectedFilters.add("vegetables&fruits");
    }


}
