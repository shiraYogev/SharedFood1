package com.example.sharedfood;

public class FoodPost {
    private String foodDescription;

    // Constructor
    // This constructor initializes the food post with a description of the food being shared.
    public FoodPost(String foodDescription) {
        this.foodDescription = foodDescription;
    }

    // Getter method for the food description
    // This method returns the description of the food shared by the user.
    public String getFoodDescription() {
        return foodDescription;
    }

    // You can add more fields here if needed, such as an image URL, user info, time of posting, etc.
}
