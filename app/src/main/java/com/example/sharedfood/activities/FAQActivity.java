package com.example.sharedfood.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sharedfood.R;

/**
 * FAQActivity handles the Frequently Asked Questions (FAQ) section.
 * It displays questions and allows users to toggle answers by clicking on the questions.
 */
public class FAQActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_faqactivity);

        // Adjust layout to fit system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize FAQ section
        setupFAQ();
    }

    /**
     * Initializes the FAQ section by setting click listeners for questions.
     * When a question is clicked, its corresponding answer will be toggled.
     */
    private void setupFAQ() {
        // Technical issues and post-related questions
        setQuestionClickListener(R.id.question1, R.id.answer1);
        setQuestionClickListener(R.id.question2, R.id.answer2);
        setQuestionClickListener(R.id.question3, R.id.answer3);
        setQuestionClickListener(R.id.question4, R.id.answer4);
        setQuestionClickListener(R.id.question5, R.id.answer5);

        // Account and personal information questions
        setQuestionClickListener(R.id.question6, R.id.answer6);
        setQuestionClickListener(R.id.question7, R.id.answer7);

        // General issues questions
        setQuestionClickListener(R.id.question8, R.id.answer8);
    }

    /**
     * Sets a click listener on a question to toggle the visibility of its corresponding answer.
     *
     * @param questionId The resource ID of the question TextView.
     * @param answerId   The resource ID of the answer TextView.
     */
    private void setQuestionClickListener(int questionId, int answerId) {
        TextView question = findViewById(questionId);
        TextView answer = findViewById(answerId);

        question.setOnClickListener(v -> {
            // Toggle answer visibility (show if hidden, hide if visible)
            if (answer.getVisibility() == View.GONE) {
                answer.setVisibility(View.VISIBLE);
            } else {
                answer.setVisibility(View.GONE);
            }
        });
    }
}
