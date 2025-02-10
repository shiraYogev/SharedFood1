package com.example.sharedfood;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class EditPostActivity extends ShareYourFoodActivity {

    private Post postToEdit;
    private String documentIdToDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // קבלת הפוסט לעריכה מהאינטנט
        postToEdit = getIntent().getParcelableExtra("POST_TO_EDIT");

        if (postToEdit != null) {
            // הסתרת כפתור בחירת תמונה אם לא רוצים לשנות תמונה
            selectImageButton.setVisibility(View.VISIBLE);

            // מילוי השדות הקיימים
            foodDescriptionEditText.setText(postToEdit.getDescription());
            cityEditText.setText(postToEdit.getCity());

            // הצגת תמונה אם קיימת בזיכרון
            if (postToEdit.getImageBase64() != null) {
                displayBase64Image(postToEdit.getImageBase64());
            } else {
                // אם התמונה לא קיימת בזיכרון הפוסט, נטען אותה מה־Firestore
                loadImageFromFirestore(postToEdit.getUserId(), postToEdit.getDescription());
            }

            // סימון הפילטרים הקיימים
            setExistingFilters(postToEdit.getFilters());

            // שינוי טקסט כפתור העלאה
            uploadPostButton.setText("עדכן פוסט");

            // מציאת מזהה המסמך למחיקה
            findDocumentIdToDelete();
        }

        // שינוי מאזין ההעלאה לעדכון פוסט
        uploadPostButton.setOnClickListener(v -> updateExistingPost());
    }

    /**
     * טוען תמונה מה־Firestore על פי userId ותיאור הפוסט
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
     * ממיר Base64 ל-Bitmap ומציג ב-ImageView
     */
    private void displayBase64Image(String base64String) {
        Bitmap bitmap = decodeBase64ToBitmap(base64String);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            Log.e("EditPost", "Failed to decode Base64 image.");
        }
    }

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

        // העתקת נתונים מהפוסט המקורי
        post.setUserId(postToEdit.getUserId());

        // העלאת הפוסט המעודכן
        super.uploadPost();
    }

    @Override
    protected void savePostToFirestore() {
        super.savePostToFirestore();

        // מחיקת הפוסט המקורי לאחר שמירת החדש
        if (documentIdToDelete != null) {
            deleteOriginalPost();
        }
    }

    private void deleteOriginalPost() {
        // מחיקת התמונה מאחסון Firebase
        if (postToEdit.getImageUrl() != null) {
            StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(postToEdit.getImageUrl());
            imageRef.delete().addOnSuccessListener(aVoid ->
                    Log.d("DeletePost", "Original image deleted successfully")
            ).addOnFailureListener(e ->
                    Log.e("DeletePost", "Failed to delete original image", e)
            );
        }

        // מחיקת המסמך מ-Firestore
        db.collection("posts").document(documentIdToDelete)
                .delete()
                .addOnSuccessListener(aVoid ->
                        Log.d("DeletePost", "Original post deleted successfully")
                )
                .addOnFailureListener(e ->
                        Log.e("DeletePost", "Failed to delete original post", e)
                );
    }

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

        // Set existing filters
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

    private void updateExistingPost() {
        // קבלת הערכים המעודכנים
        String updatedDescription = foodDescriptionEditText.getText().toString().trim();
        String updatedCity = cityEditText.getText().toString().trim();

        if (updatedDescription.isEmpty()) {
            Toast.makeText(this, "אנא הזן תיאור", Toast.LENGTH_SHORT).show();
            return;
        }

        // עדכון הפילטרים
        updateSelectedFilters();

        // מציאת הפוסט במסד הנתונים ועדכונו
        db.collection("posts")
                .whereEqualTo("userId", postToEdit.getUserId())
                .whereEqualTo("description", postToEdit.getDescription())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // יצירת מפת עדכונים
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("description", updatedDescription);
                        updates.put("city", updatedCity);
                        updates.put("filters", selectedFilters);

                        // עדכון המסמך
                        document.getReference().update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "הפוסט עודכן בהצלחה", Toast.LENGTH_SHORT).show();
                                    navigateToMyPosts();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "עדכון הפוסט נכשל", Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "שגיאה בחיפוש הפוסט לעדכון", Toast.LENGTH_SHORT).show());
    }

    private void navigateToMyPosts() {
        Intent intent = new Intent(this, MyPostsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private Bitmap decodeBase64ToBitmap(String base64String) {
        byte[] decodedBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

}