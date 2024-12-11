package com.example.sharedfood;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class HomePageActivity extends AppCompatActivity {

    private Button shareFoodButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        // Initialize the button to share food
        shareFoodButton = findViewById(R.id.shareFoodButton);

        // Navigate to ShareYourFoodActivity when the Share Food button is clicked
        shareFoodButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomePageActivity.this, ShareYourFoodActivity.class);
            startActivity(intent);
        });
    }
}
