package com.example.sharedfood;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ShareYourFoodActivity extends AppCompatActivity {

    private EditText foodDescriptionEditText;  // EditText to input the food description
    private Button shareFoodButton, uploadPhotoButton, selectImageButton;  // Buttons for sharing food and uploading photos
    private FirebaseFirestore db;  // Firestore instance to interact with Firebase database
    private FirebaseStorage storage;  // Firebase storage instance to upload the image
    private StorageReference storageReference;  // Storage reference to Firebase storage
    private Uri imageUri;  // URI of the selected image
    private ImageView imageView;  // ImageView to display the selected image
    private boolean isKosher, isHot, isCold, isClosed, isDairy, isMeat;  // The properties of the food

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_your_food);  // Set the layout for this activity

        // Initialize the views by finding them by their IDs in the layout
        foodDescriptionEditText = findViewById(R.id.foodDescriptionEditText);
        shareFoodButton = findViewById(R.id.shareFoodButton);
        uploadPhotoButton = findViewById(R.id.selectImageButton);
        selectImageButton = findViewById(R.id.selectImageButton);
        imageView = findViewById(R.id.imageView);

        // Initialize Firestore and FirebaseStorage instances
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        // Set up the "Select Image" button's click listener to show the image source dialog
        selectImageButton.setOnClickListener(v -> showImageSourceDialog());

        // Set up the "Share Food" button's click listener to share the food post
        shareFoodButton.setOnClickListener(v -> {
            String foodDescription = foodDescriptionEditText.getText().toString().trim();

            if (!foodDescription.isEmpty()) {
                // Create a new FoodPost object with the description and properties (including image URL)
                FoodPost foodPost = new FoodPost(foodDescription, isKosher, isHot, isCold, isClosed, isDairy, isMeat, null);

                // If an image is selected, upload it to Firebase
                if (imageUri != null) {
                    uploadImageToFirebase(foodPost);
                } else {
                    // If no image, just save the food post without an image
                    saveFoodPost(foodPost, null);
                }
            } else {
                Toast.makeText(ShareYourFoodActivity.this, "Please enter a description", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up the "Upload Photo" button's click listener (future feature, not implemented yet)
        uploadPhotoButton.setOnClickListener(v -> {
            Toast.makeText(ShareYourFoodActivity.this, "Photo upload feature coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    // Function to display a dialog for selecting either camera or gallery
    private void showImageSourceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ShareYourFoodActivity.this);
        builder.setTitle("Select Image Source")
                .setItems(new CharSequence[] {"Camera", "Gallery"}, (dialog, which) -> {
                    if (which == 0) {
                        // If "Camera" is selected, open the camera
                        openCamera();
                    } else {
                        // If "Gallery" is selected, open the gallery
                        openGallery();
                    }
                })
                .show();
    }

    // Function to open the camera and take a photo
    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, 100); // 100 for camera request code
        }
    }

    // Function to open the gallery and choose an image
    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, 200); // 200 for gallery request code
    }

    // This function handles the result after the camera or gallery action
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check if the result is OK (image was selected or captured)
        if (resultCode == RESULT_OK) {
            if (requestCode == 100) { // Camera
                // If the photo was taken from the camera, get the image
                if (data != null && data.getExtras() != null) {
                    imageUri = (Uri) data.getExtras().get("data");
                    imageView.setImageURI(imageUri); // Display the image in an ImageView
                }
            } else if (requestCode == 200) { // Gallery
                // If the photo was selected from the gallery, get the image URI
                imageUri = data.getData();
                imageView.setImageURI(imageUri); // Display the image in an ImageView
            }
        }
    }

    // Function to upload the image to Firebase Storage
    private void uploadImageToFirebase(FoodPost foodPost) {
        if (imageUri != null) {
            StorageReference imageRef = storageReference.child("foodImages/" + System.currentTimeMillis() + ".jpg");

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // On success, get the image URL
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            // Save the food post with the image URL
                            saveFoodPost(foodPost, imageUrl);
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ShareYourFoodActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // If no image is selected, just save the food post without an image
            saveFoodPost(foodPost, null);
        }
    }

    // Function to save the food post to Firestore
    private void saveFoodPost(FoodPost foodPost, String imageUrl) {
        if (imageUrl != null) {
            foodPost = new FoodPost(foodPost.getFoodDescription(), foodPost.isKosher(), foodPost.isHot(),
                    foodPost.isCold(), foodPost.isClosed(), foodPost.isDairy(), foodPost.isMeat(), imageUrl);
        }

        db.collection("foodPosts")
                .add(foodPost)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(ShareYourFoodActivity.this, "Food shared successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Return to the home screen
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ShareYourFoodActivity.this, "Failed to share food", Toast.LENGTH_SHORT).show();
                });
    }
}
