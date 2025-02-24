/**
 * ShareYourFoodActivity
 *
 * This activity allows users to share food posts with the community.
 * Users can upload an image from camera or gallery, add a description,
 * select relevant filters (kosher, vegan, temperature, etc.),
 * and specify the city location.
 *
 * The activity handles permission requests, image selection/capture,
 * conflict validation between filters, and uploading posts to Firebase.
 */
package com.example.sharedfood.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import android.util.Base64;
import java.io.ByteArrayOutputStream;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.InputStream;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.sharedfood.R;
import com.example.sharedfood.post.Post;
import com.google.firebase.firestore.GeoPoint;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class ShareYourFoodActivity extends AppCompatActivity {

    // Permission and request codes
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 101;
    private static final int GALLERY_PERMISSION_REQUEST_CODE = 102;
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int GALLERY_REQUEST_CODE = 200;

    // UI elements
    protected EditText foodDescriptionEditText;
    protected Button uploadPostButton, selectImageButton;
    protected FirebaseFirestore db;
    protected FirebaseStorage storage;
    protected StorageReference storageRef;
    protected Uri imageUri;
    protected ImageView imageView;
    private FusedLocationProviderClient fusedLocationClient;

    // Food attributes and filters
    protected List<String> selectedFilters = new ArrayList<>();
    protected Post post;
    protected EditText cityEditText;
    protected CheckBox extraKosherCheckBox, frizerCheckBox, pastriesCheckBox,
            vegetablesCheckBox, kosherCheckBox, veganCheckBox, vegetarianCheckBox,
            glutenFreeCheckBox, hotCheckBox, coldCheckBox, closedCheckBox,
            dairyCheckBox, meatCheckBox;

    /**
     * Activity result launcher for gallery image selection
     * Handles setting the selected image to the ImageView and storing in the post object
     */
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    if (imageUri != null) {
                        if (post == null) post = new Post();
                        post.setImageUri(imageUri);
                        imageView.setImageURI(imageUri);
                    }
                }
            }
    );

    /**
     * Activity result launcher for camera image capture
     * Handles setting the captured image to the ImageView and storing in the post object
     */
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (post == null) post = new Post();
                    // If camera URI is predefined
                    if (imageUri != null) {
                        post.setImageUri(imageUri);
                        imageView.setImageURI(imageUri);
                    }
                }
            }
    );

    /**
     * Initialize the activity, set up UI elements and attach event listeners
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_your_food);

        // Initialize fields
        cityEditText = findViewById(R.id.cityEditText);

        kosherCheckBox = findViewById(R.id.kosherCheckBox);
        hotCheckBox = findViewById(R.id.hotCheckBox);
        coldCheckBox = findViewById(R.id.coldCheckBox);
        closedCheckBox = findViewById(R.id.closedCheckBox);
        dairyCheckBox = findViewById(R.id.dairyCheckBox);
        meatCheckBox = findViewById(R.id.meatCheckBox);
        veganCheckBox = findViewById(R.id.veganCheckBox);
        vegetarianCheckBox = findViewById(R.id.vegetarianCheckBox);
        glutenFreeCheckBox = findViewById(R.id.glutenFreeCheckBox);

        extraKosherCheckBox = findViewById(R.id.extraKosherCheckBox);
        frizerCheckBox = findViewById(R.id.frizerCheckBox);
        pastriesCheckBox = findViewById(R.id.pastriesCheckBox);
        vegetablesCheckBox = findViewById(R.id.vegetablesCheckBox);

        // Initialize views
        foodDescriptionEditText = findViewById(R.id.foodDescriptionEditText);
        uploadPostButton = findViewById(R.id.uploadPostButton);
        selectImageButton = findViewById(R.id.selectImageButton);
        imageView = findViewById(R.id.imageView);

        // Initialize Firebase services
        //FirebaseApp.initializeApp(ShareYourFoodActivity.this);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // Set up button click listeners
        selectImageButton.setOnClickListener(v -> showImageSourceDialog());

        // Share Food button listener
        uploadPostButton.setOnClickListener(v -> {

            // Check for filter conflicts
            if (checkForConflictingFilters()) {
                return; // If there's a conflict, stop the upload
            }

            // Get user input
            String foodDescription = foodDescriptionEditText.getText().toString().trim();
            String city = cityEditText.getText().toString().trim();
            if (foodDescription.isEmpty()) {
                Toast.makeText(this, "Please enter a description", Toast.LENGTH_SHORT).show();
                return;
            }
            else {
                post.setDescription(foodDescription);
                if (!city.isEmpty()) {
                    setGeoLocation(city); // Save location based on city
                }
                post.setCity(city);
                post.setImageUri(imageUri);
                updateSelectedFilters();
                setUserIdForPost();
            }

            uploadPost();
        });
    }

    /**
     * Converts city name to geographic coordinates using Geocoder
     * Stores the resulting GeoPoint in the post object
     *
     * @param city The name of the city to geolocate
     */
    protected void setGeoLocation(String city) {
        Geocoder geocoder = new Geocoder(this, new Locale("he", "IL"));  // Set locale to Hebrew
        try {
            List<Address> addresses = geocoder.getFromLocationName(city, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                double latitude = address.getLatitude();
                double longitude = address.getLongitude();

                // Convert location to GeoPoint
                GeoPoint location = new GeoPoint(latitude, longitude);
                post.setLocation(location); // Save to post location
            } else {
                Toast.makeText(this, "City not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error geocoding city", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Updates the selected filters list based on checkbox states
     * Clears existing filters first, then adds all selected filters
     */
    protected void updateSelectedFilters() {
        if (post.getFilters() != null) {
            post.getFilters().clear();  // Clear list first
        }
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

        post.setFilters(selectedFilters);
    }

    /**
     * Checks for conflicting filter selections
     * Returns true if conflicts are found, false otherwise
     *
     * @return boolean indicating whether conflicts exist
     */
    protected boolean checkForConflictingFilters() {
        // Check for kosher conflicts
        if ((kosherCheckBox.isChecked() || !extraKosherCheckBox.isChecked()) &&
                (meatCheckBox.isChecked() && dairyCheckBox.isChecked())) {
            showConflictMessage("לא ניתן לבחור 'כשר' עם מרכיבים לא כשרים.");
            return true;
        }

        // Check for meat and vegetarian/vegan conflicts
        if ((meatCheckBox.isChecked() && vegetarianCheckBox.isChecked()) ||
                (meatCheckBox.isChecked() && veganCheckBox.isChecked())) {
            showConflictMessage("לא ניתן לבחור גם בשרי וגם צמחוני או טבעוני.");
            return true;
        }

        // Check for temperature conflicts (hot, cold, frozen)
        if ((hotCheckBox.isChecked() && coldCheckBox.isChecked()) ||
                (hotCheckBox.isChecked() && frizerCheckBox.isChecked()) ||
                (coldCheckBox.isChecked() && frizerCheckBox.isChecked())) {
            showConflictMessage("לא ניתן לבחור 'חם', 'קר' ו'קפוא' יחד.");
            return true;
        }

        // No conflicts found
        return false;
    }

    /**
     * Displays an alert dialog with conflict message
     *
     * @param message The conflict message to display
     */
    private void showConflictMessage(String message) {
        // Show message with alert icon
        new AlertDialog.Builder(this)
                .setTitle("יש בעיה עם הבחירות שלך")
                .setMessage(message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("אוקי", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }

    /**
     * Shows a dialog for selecting image source (camera or gallery)
     */
    protected void showImageSourceDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Select Image Source")
                .setItems(new CharSequence[] {"Camera", "Gallery"}, (dialog, which) -> {
                    if (which == 0) {
                        checkCameraPermission();
                    } else {
                        checkGalleryPermission();
                    }
                })
                .show();
    }

    /**
     * Checks if camera permission is granted, requests if not
     * Opens camera if permission is granted
     */
    protected void checkCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Android 10 and above
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        CAMERA_PERMISSION_REQUEST_CODE);
            } else {
                openCamera();
            }
        } else { // Older versions
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        CAMERA_PERMISSION_REQUEST_CODE);
            } else {
                openCamera();
            }
        }
    }

    /**
     * Checks if gallery permission is granted, requests if not
     * Opens gallery if permission is granted
     * Handles different permissions for Android 13+ vs older versions
     */
    protected void checkGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        GALLERY_PERMISSION_REQUEST_CODE);
            } else {
                openGallery();
            }
        } else { // For versions below Android 13
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        GALLERY_PERMISSION_REQUEST_CODE);
            } else {
                openGallery();
            }
        }
    }

    /**
     * Opens the camera to take a picture
     * Creates a temporary file for storing the image
     * Uses FileProvider for secure file handling
     */
    protected void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = createImageFile();
            if (photoFile != null) {
                // Use FileProvider instead of Uri.fromFile
                imageUri = androidx.core.content.FileProvider.getUriForFile(
                        this,
                        "com.example.sharedfood.fileprovider",  // Use your package name + .fileprovider
                        photoFile
                );
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

                // Important: Add read/write permissions for camera app
                takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                try {
                    cameraLauncher.launch(takePictureIntent);
                } catch (Exception e) {
                    Log.e("Camera", "Error opening camera: " + e.getMessage());
                    Toast.makeText(this, "Error opening camera", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Opens the gallery to select an image
     */
    protected void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");
        galleryLauncher.launch(galleryIntent);
    }

    /**
     * Creates a temporary file for storing camera images
     *
     * @return File object for the temporary image
     */
    protected File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            return File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Handles permission request results
     * Opens camera or gallery if permissions are granted
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == GALLERY_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Gallery permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Uploads the post image to Firebase Storage
     * Then saves post data to Firestore
     */
    protected void uploadPost() {
        if (post == null || post.getImageUri() == null) {
            Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
            return;
        }

        storageRef = FirebaseStorage.getInstance().getReference().child("foodImages/" + UUID.randomUUID().toString() + ".jpg");
        storageRef.putFile(post.getImageUri()).addOnSuccessListener(taskSnapshot -> {
            Log.d("Firebase", "Upload successful");
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                Log.d("Firebase", "Download URL received: " + uri);
                post.setImageUrl(uri.toString());
                savePostToFirestore();
            }).addOnFailureListener(e -> {
                Log.e("Firebase", "Failed to get download URL", e);
            });
        }).addOnFailureListener(e -> {
            Log.e("Firebase", "Image upload failed", e);
        });
        savePostToFirestore();
    }

    /**
     * Saves the post data to Firestore
     * Compresses the image to base64 for storage
     * Navigates to homepage after successful upload
     */
    protected void savePostToFirestore() {
        // Reference to Firestore
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        if (post.getImageUri() != null) {
            try {
                // Convert image to Base64
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), post.getImageUri());
                String base64Image = compressImageToBase64(bitmap);
                post.setImageBase64(base64Image); // Ensure field exists in Post class
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to compress image", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Create post object
        Map<String, Object> foodPost = new HashMap<>();
        foodPost.put("description", post.getDescription());
        foodPost.put("filters", post.getFilters());
        foodPost.put("imageUri", post.getImageUri());
        foodPost.put("imageBase64", post.getImageBase64()); // Save Base64
        foodPost.put("userId", post.getUserId());
        foodPost.put("location", post.getLocation());
        foodPost.put("city", post.getCity());
        foodPost.put("timestamp", System.currentTimeMillis());

        // Save post to "posts" collection
        firestore.collection("posts")
                .add(foodPost)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Firestore", "Post uploaded successfully with ID: " + documentReference.getId());
                    Toast.makeText(this, "Post uploaded successfully", Toast.LENGTH_SHORT).show();
                    navigateToHomePage(); // Navigate to HomePage
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Post upload failed", e);
                    Toast.makeText(this, "Post upload failed", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Navigates to the Home Page activity
     * Clears activity stack to prevent going back to upload page
     */
    protected void navigateToHomePage() {
        Intent intent = new Intent(ShareYourFoodActivity.this, HomePageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // Finish current activity
    }

    /**
     * Sets the current user ID to the post
     * Gets user ID from Firebase Authentication
     */
    protected void setUserIdForPost() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid(); // Current user ID
            post.setUserId(userId); // Save user ID to post
        } else {
            Log.e("ShareYourFoodActivity", "User not logged in");
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Converts image URI to Base64 string
     *
     * @param imageUri URI of the image to convert
     * @return Base64 encoded string of the image
     */
    protected String convertImageToBase64(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            byte[] byteArray = outputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Compresses a bitmap image and converts to Base64 string
     * Uses 50% compression quality to reduce size
     *
     * @param bitmap The bitmap to compress
     * @return Base64 encoded string of the compressed image
     */
    protected String compressImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream); // 50 = compression level
        byte[] byteArray = outputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}