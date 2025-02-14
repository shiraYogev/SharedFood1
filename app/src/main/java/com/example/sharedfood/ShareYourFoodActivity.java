package com.example.sharedfood;

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

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 101;
    private static final int GALLERY_PERMISSION_REQUEST_CODE = 102;
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int GALLERY_REQUEST_CODE = 200;

    protected EditText foodDescriptionEditText;
    protected Button uploadPostButton , selectImageButton;
    protected FirebaseFirestore db;
    protected FirebaseStorage storage;
    protected StorageReference storageRef;
    protected Uri imageUri;
    protected ImageView imageView;
    private FusedLocationProviderClient fusedLocationClient;

    protected List<String> selectedFilters = new ArrayList<>();
    protected Post post;
    protected EditText cityEditText;
    protected CheckBox extraKosherCheckBox, frizerCheckBox, pastriesCheckBox,
            vegetablesCheckBox, kosherCheckBox,veganCheckBox, vegetarianCheckBox,
            glutenFreeCheckBox, hotCheckBox, coldCheckBox, closedCheckBox,
            dairyCheckBox, meatCheckBox;

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

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (post == null) post = new Post();
                    // אם יש לך URI מוגדר מראש למצלמה
                    if (imageUri != null) {
                        post.setImageUri(imageUri);
                        imageView.setImageURI(imageUri);
                    }
                }
            }
    );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_your_food);

        // אתחול השדות
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
        uploadPostButton  = findViewById(R.id.uploadPostButton );
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

            // בדיקת הסתירות
            if (checkForConflictingFilters()) {
                return; // אם יש סתירה, מפסיקים את ההעלאה
            }

            // קבלת העיר מהקלט של המשתמש
            String foodDescription = foodDescriptionEditText.getText().toString().trim();
            String city = cityEditText.getText().toString().trim();
            if (foodDescription.isEmpty()) {
                Toast.makeText(this, "Please enter a description", Toast.LENGTH_SHORT).show();
                return;
            }
            else{
                post.setDescription(foodDescription);
                if (!city.isEmpty()) {
                    setGeoLocation(city); // שמירה של המיקום לפי העיר
                }
                post.setCity(city);
                post.setImageUri(imageUri);
                updateSelectedFilters();
                setUserIdForPost();

            }

         uploadPost();
        });
    }

    // הפונקציה המתבצעת כדי להמיר את העיר ל-GPS

    protected void setGeoLocation(String city) {
        Geocoder geocoder = new Geocoder(this, new Locale("he", "IL"));  // הגדרת Locale לעברית
        try {
            List<Address> addresses = geocoder.getFromLocationName(city, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                double latitude = address.getLatitude();
                double longitude = address.getLongitude();

                // המרת המיקום ל-GeoPoint
                GeoPoint location = new GeoPoint(latitude, longitude);
                post.setLocation(location); // שמירה במיקום של הפוסט
            } else {
                Toast.makeText(this, "City not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error geocoding city", Toast.LENGTH_SHORT).show();
        }
    }


    protected void updateSelectedFilters() {
        if(post.getFilters()!=null) {
            post.getFilters().clear();  // מנקה את הרשימה קודם כל
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

    protected boolean checkForConflictingFilters() {
        // בודק סתירות בין הפילטרים שנבחרו
        if ((kosherCheckBox.isChecked() || !extraKosherCheckBox.isChecked()) &&
                (meatCheckBox.isChecked() && dairyCheckBox.isChecked())) {
            showConflictMessage("לא ניתן לבחור 'כשר' עם מרכיבים לא כשרים.");
            return true;
        }

        if ((meatCheckBox.isChecked() && vegetarianCheckBox.isChecked()) ||
                (meatCheckBox.isChecked() && veganCheckBox.isChecked())) {
            showConflictMessage("לא ניתן לבחור גם בשרי וגם צמחוני או טבעוני.");
            return true;
        }

        // בדיקה אם נבחרים יחד "חם", "קר" ו"קפוא" (קונפליקט בין פילטרים חמים, קרים וקפואים)
        if ((hotCheckBox.isChecked() && coldCheckBox.isChecked()) ||
                (hotCheckBox.isChecked() && frizerCheckBox.isChecked()) ||
                (coldCheckBox.isChecked() && frizerCheckBox.isChecked())) {
            showConflictMessage("לא ניתן לבחור 'חם', 'קר' ו'קפוא' יחד.");
            return true;
        }

        // אם אין סתירה, מחזירים false
        return false;
    }

    private void showConflictMessage(String message) {
        // הצגת הודעה עם עיצוב ירוק ואייקון
        new AlertDialog.Builder(this)
                .setTitle("יש בעיה עם הבחירות שלך")
                .setMessage(message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("אוקי", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }


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

    protected void checkCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Android 10 ומעלה
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        CAMERA_PERMISSION_REQUEST_CODE);
            } else {
                openCamera();
            }
        } else { // גרסאות ישנות יותר
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

    protected void checkGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // גרסה 13 ומעלה
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        GALLERY_PERMISSION_REQUEST_CODE);
            } else {
                openGallery();
            }
        } else { // לגרסאות פחות מ-13
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

    protected void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = createImageFile();
            if (photoFile != null) {
                imageUri = Uri.fromFile(photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                try {
                    cameraLauncher.launch(takePictureIntent);
                } catch (Exception e) {
                    Toast.makeText(this, "Error opening camera", Toast.LENGTH_SHORT).show();
                }
                cameraLauncher.launch(takePictureIntent);
            }
        } else {
            Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show();
        }
    }

    protected void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");
        galleryLauncher.launch(galleryIntent);
    }

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


    protected void savePostToFirestore() {
        // הפניה ל-Firestore
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        if (post.getImageUri() != null) {
            try {
                // המרת התמונה ל-Base64
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), post.getImageUri());
                String base64Image = compressImageToBase64(bitmap);
                post.setImageBase64(base64Image); // ודאי שהשדה נוסף למחלקת Post
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to compress image", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // יצירת אובייקט פוסט
        Map<String, Object> foodPost = new HashMap<>();
        foodPost.put("description", post.getDescription());
        foodPost.put("filters", post.getFilters());
        foodPost.put("imageUri", post.getImageUri());
        foodPost.put("imageBase64", post.getImageBase64()); // שמירת Base64
        foodPost.put("userId", post.getUserId());
        foodPost.put("location", post.getLocation());
        foodPost.put("city", post.getCity());
        foodPost.put("timestamp", System.currentTimeMillis());

        // שמירת הפוסט בקולקציה "posts"
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


    protected void navigateToHomePage() {
        Intent intent = new Intent(ShareYourFoodActivity.this, HomePageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // מסיים את האקטיביטי הנוכחי
    }


    protected void setUserIdForPost() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid(); // מזהה המשתמש הנוכחי
            post.setUserId(userId); // שמירה של ה-ID של המשתמש בפוסט
        } else {
            Log.e("ShareYourFoodActivity", "User not logged in");
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }


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

    protected String compressImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream); // 50 = רמת דחיסה
        byte[] byteArray = outputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}

