package com.example.sharedfood;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class HomePageActivity extends AppCompatActivity {

    private Button shareFoodButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        shareFoodButton = findViewById(R.id.shareFoodButton);

        // כשלוחצים על "Share Food", עוברים לדף של שיתוף אוכל
        shareFoodButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomePageActivity.this, ShareYourFoodActivity.class);
            startActivity(intent);
        });

    }
}
