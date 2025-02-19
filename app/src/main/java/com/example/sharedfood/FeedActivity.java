package com.example.sharedfood;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FeedActivity extends AppCompatActivity {
    protected RecyclerView recyclerView;
    protected PostAdapter adapter;
    protected FirebaseFirestore db;
    protected TextView emptyStateText;
    private EditText cityInput;
    protected List<Post> postsList;

    // Filters CheckBoxes
    protected CheckBox extraKosherCheckBox, frizerCheckBox, pastriesCheckBox,
            vegetablesCheckBox, kosherCheckBox,veganCheckBox, vegetarianCheckBox,
            glutenFreeCheckBox, hotCheckBox, coldCheckBox, closedCheckBox,
            dairyCheckBox, meatCheckBox;


    private static final String TAG = "FeedActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize views
        recyclerView = findViewById(R.id.postsRecyclerView);
        emptyStateText = findViewById(R.id.emptyStateText);
        cityInput = findViewById(R.id.cityInput);
        postsList = new ArrayList<>();

        // Initialize filters
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

        setupRecyclerView();

        // Add listeners to filters and city input
        setupFilterListeners();
        setupCityInputListener();

        // Load all posts initially
        loadPosts("");
    }

    /**
     * Sets up the RecyclerView with a LinearLayoutManager and adapter.
     */
    protected void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PostAdapter(postsList, this);  // הקונסטרוקטור מצפה ל-String בנוסף ל-List ו-Context
        recyclerView.setAdapter(adapter);
    }

    /**
     * Listens for user input in the city search field and updates the post list.
     */
    protected void setupCityInputListener() {
        cityInput.setOnEditorActionListener((v, actionId, event) -> {
            String city = cityInput.getText().toString().trim();
            loadPosts(city);
            return true;
        });
    }

    /**
     * Fetches posts from Firestore and applies city and filter-based filtering.
     *
     * @param city the city to filter posts by
     */
    protected void loadPosts(String city) {
        db.collection("posts")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        postsList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Post post = new Post();
                                post.setDescription(document.getString("description"));
                                post.setUserId(document.getString("userId"));

                                // Decode image from Base64
                                String base64Image = document.getString("imageBase64");
                                if (base64Image != null) {
                                    Bitmap bitmap = decodeBase64ToBitmap(base64Image);
                                    post.setImageBitmap(bitmap);
                                }

                                post.setFilters((List<String>) document.get("filters"));
                                post.setCity(document.getString("city"));

                                // Filter by city
                                if (!city.isEmpty() && (post.getCity() == null || !post.getCity().toLowerCase().contains(city.toLowerCase()))) {
                                    continue;
                                }

                                // Check if the post matches selected filters
                                if (isPostMatchingFilters(post)) {
                                    postsList.add(post);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing document: " + e.getMessage(), e);
                            }
                        }

                        updateEmptyState();
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                        Toast.makeText(this, "Failed to load posts. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Adds listeners to all filter checkboxes to reload posts when toggled.
     */
    protected void setupFilterListeners() {
        CheckBox[] checkBoxes = {
                extraKosherCheckBox, frizerCheckBox, pastriesCheckBox, vegetablesCheckBox,
                kosherCheckBox,veganCheckBox, vegetarianCheckBox, glutenFreeCheckBox, hotCheckBox,
                coldCheckBox, closedCheckBox, dairyCheckBox, meatCheckBox
        };

        for (CheckBox checkBox : checkBoxes) {
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> loadPosts(cityInput.getText().toString().trim()));
        }
    }

    /**
     * Checks if a post matches the selected filters.
     *
     * @param post the post to check
     * @return true if the post matches all selected filters
     */
    protected boolean isPostMatchingFilters(Post post) {
        if (kosherCheckBox.isChecked() && !post.hasFilter("Kosher")) return false;
        if (veganCheckBox.isChecked() && !post.hasFilter("vegan")) return false;
        if (vegetarianCheckBox.isChecked() && !post.hasFilter("vegetarian")) return false;
        if (glutenFreeCheckBox.isChecked() && !post.hasFilter("glutenFree")) return false;
        if (hotCheckBox.isChecked() && !post.hasFilter("Hot")) return false;
        if (coldCheckBox.isChecked() && !post.hasFilter("Cold")) return false;
        if (closedCheckBox.isChecked() && !post.hasFilter("Closed")) return false;
        if (dairyCheckBox.isChecked() && !post.hasFilter("Dairy")) return false;
        if (meatCheckBox.isChecked() && !post.hasFilter("Meat")) return false;
        if (extraKosherCheckBox.isChecked() && !post.hasFilter("extraKosher")) return false;
        if (frizerCheckBox.isChecked() && !post.hasFilter("frizer")) return false;
        if (pastriesCheckBox.isChecked() && !post.hasFilter("pastries")) return false;
        if (vegetablesCheckBox.isChecked() && !post.hasFilter("vegetables")) return false;

        return true;
    }

    /**
     * Updates the UI to show or hide the empty state message.
     */
    protected void updateEmptyState() {
        if (postsList.isEmpty()) {
            emptyStateText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyStateText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Decodes a Base64 string to a Bitmap image.
     *
     * @param base64String the Base64 encoded string
     * @return the decoded Bitmap, or null if decoding fails
     */
    protected Bitmap decodeBase64ToBitmap(String base64String) {
        try {
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            Log.e(TAG, "Failed to decode Base64 string to Bitmap: " + e.getMessage(), e);
            return null;
        }
    }
}
