package com.example.sharedfood;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class FAQActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_faqactivity);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // שאלות ותשובות
        setupFAQ();
    }

    private void setupFAQ() {
        // שאלות בעיות טכניות ושאלות על פוסטים
        setQuestionClickListener(R.id.question1, R.id.answer1);
        setQuestionClickListener(R.id.question2, R.id.answer2);
        setQuestionClickListener(R.id.question3, R.id.answer3);
        setQuestionClickListener(R.id.question4, R.id.answer4);
        setQuestionClickListener(R.id.question5, R.id.answer5);

        // שאלות חשבונות ומידע אישי
        setQuestionClickListener(R.id.question6, R.id.answer6);
        setQuestionClickListener(R.id.question7, R.id.answer7);

        // שאלות בעיות כלשהן
        setQuestionClickListener(R.id.question8, R.id.answer8);
    }

    // פונקציה ל-ClickListener
    private void setQuestionClickListener(int questionId, int answerId) {
        TextView question = findViewById(questionId);
        TextView answer = findViewById(answerId);

        question.setOnClickListener(v -> {
            // הצגת או הסתרת התשובה לפי הצורך
            if (answer.getVisibility() == View.GONE) {
                answer.setVisibility(View.VISIBLE);
            } else {
                answer.setVisibility(View.GONE);
            }
        });
    }
}
