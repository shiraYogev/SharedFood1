package com.example.sharedfood.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sharedfood.R;
import com.example.sharedfood.post.MyPostsAdapter;
import com.example.sharedfood.post.Post;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

// Michel START 14/1/2025
/**
 * Activity class for displaying and managing the user's personal posts.
 * Implements listeners for post deletion and editing.
 */
public class MyPostsActivity extends AppCompatActivity implements MyPostsAdapter.PostDeleteListener, MyPostsAdapter.PostEditListener {
    // Michael END 14/1/2025
    private RecyclerView recyclerView;
    private MyPostsAdapter adapter;
    FirebaseFirestore db;
    FirebaseAuth auth;
    private TextView emptyStateText;
    private List<Post> postsList;
    private String currentUserId;
    Post post;
    private static final String TAG = "MyPostsActivity";

    /**
     * Initializes the activity, sets up UI components, and loads the user's posts.
     * @param savedInstanceState The saved state of the activity, if any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        // Initialize Firebase instances
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize views and lists
        recyclerView = findViewById(R.id.postsRecyclerView);
        emptyStateText = findViewById(R.id.emptyStateText);
        postsList = new ArrayList<>();

        setupRecyclerView();
        setupAddButton();
        loadUserPosts();
    }

    /**
     * Configures the RecyclerView with a LinearLayoutManager and adapter.
     */
    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyPostsAdapter(postsList, this, this); // Michael START-END 14.01.2025
        recyclerView.setAdapter(adapter);
    }

    /**
     * Sets up the FloatingActionButton to navigate to the post creation activity.
     */
    private void setupAddButton() {
        FloatingActionButton fabAddPost = findViewById(R.id.fabAddPost);
        fabAddPost.setOnClickListener(v -> startActivity(new Intent(MyPostsActivity.this, ShareYourFoodActivity.class)));
    }

    /**
     * Loads the current user's posts from Firestore and populates the posts list.
     */
    private void loadUserPosts() {
        if (auth.getCurrentUser() == null) return;

        currentUserId = auth.getCurrentUser().getUid();

        db.collection("posts")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        postsList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                // Create a new Post object from the document
                                Post post = new Post();

                                // Copy basic data
                                post.setUserId(document.getString("userId"));
                                post.setDescription(document.getString("description"));

                                // Handle image reconstruction from Base64
                                String base64Image = document.getString("imageBase64");
                                if (base64Image != null) {
                                    Bitmap bitmap = decodeBase64ToBitmap(base64Image);
                                    post.setImageBitmap(bitmap); // Ensure this field exists in the Post class
                                }

                                // Handle filters list
                                @SuppressWarnings("unchecked")
                                List<String> filters = (List<String>) document.get("filters");
                                post.setFilters(filters);

                                // Handle image URL
                                String imageUrl = document.getString("imageUrl");
                                post.setImageUrl(imageUrl);

                                // Handle image URI - convert String to Uri
                                String imageUriString = document.getString("imageUri");
                                if (imageUriString != null && !imageUriString.isEmpty()) {
                                    post.setImageUri(Uri.parse(imageUriString));
                                }

                                // Handle location
                                GeoPoint geoPoint = document.getGeoPoint("location");
                                if (geoPoint != null) {
                                    post.setLocation(geoPoint);
                                }

                                // Handle city
                                String city = document.getString("city");
                                post.setCity(city);

                                postsList.add(post);

                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing document to Post: " + e.getMessage());
                            }
                        }

                        updateEmptyState();
                        adapter.notifyDataSetChanged();

                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                        Toast.makeText(MyPostsActivity.this, "שגיאה בטעינת הפוסטים", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Updates the visibility of the RecyclerView and empty state text based on the posts list.
     */
    private void updateEmptyState() {
        if (postsList.isEmpty()) {
            emptyStateText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyStateText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Handles the edit action for a post by launching the EditPostActivity.
     * @param post The post to be edited.
     */
    @Override
    public void onEditClick(Post post) {
        Intent intent = new Intent(this, EditPostActivity.class);
        intent.putExtra("POST_TO_EDIT", post);
        startActivity(intent);
    }

    /**
     * Displays a confirmation dialog for deleting a post.
     * @param post The post to be deleted.
     */
    @Override
    public void onDeleteClick(Post post) {
        new AlertDialog.Builder(this)
                .setTitle("מחיקת פוסט")
                .setMessage("האם את/ה בטוח/ה שברצונך למחוק את הפוסט?")
                .setPositiveButton("מחק", (dialog, which) -> deletePost(post))
                .setNegativeButton("ביטול", null)
                .show();
    }

    /**
     * Deletes a specific post from Firestore based on user ID and description.
     * @param post The post to delete.
     */
    private void deletePost(Post post) {
        if (auth.getCurrentUser() == null) return;

        String currentUserId = auth.getCurrentUser().getUid();

        db.collection("posts")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("description", post.getDescription())
                .get()
                .addOnSuccessListener(documents -> {
                    for (DocumentSnapshot document : documents) {
                        document.getReference().delete()
                                .addOnSuccessListener(aVoid ->
                                        Toast.makeText(MyPostsActivity.this, "הפוסט נמחק בהצלחה", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                        Toast.makeText(MyPostsActivity.this, "שגיאה במחיקת הפוסט", Toast.LENGTH_SHORT).show());
                    }
                });
    }

    /**
     * Decodes a Base64 string into a Bitmap image (unused duplicate method).
     * @param base64String The Base64 string to decode.
     * @return The decoded Bitmap or null if decoding fails.
     */
    private Bitmap decodeBase64ToImage(String base64String) {
        try {
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            Log.e("DecodeBase64", "Error decoding Base64: " + e.getMessage());
            return null;
        }
    }

    /**
     * Reads a Base64 string from a file.
     * @param filePath The path to the file containing the Base64 string.
     * @return The Base64 string or null if reading fails.
     */
    public String readBase64FromFile(String filePath) {
        try {
            File file = new File(filePath);
            byte[] encoded = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                encoded = Files.readAllBytes(file.toPath());
            }
            return new String(encoded, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Decodes a Base64 string into a Bitmap image.
     * @param base64String The Base64 string to decode.
     * @return The decoded Bitmap or null if decoding fails.
     */
    private Bitmap decodeBase64ToBitmap(String base64String) {
        try {
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}