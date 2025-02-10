package com.example.sharedfood;

import android.graphics.Bitmap;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;
import com.google.firebase.firestore.GeoPoint;

import android.os.Parcel;
import android.os.Parcelable;

public class Post implements Parcelable {
    // הגדרת השדות כ- final כדי למנוע מהם שינוי אחרי יצירת האובייקט
    private String userId;
    private String description;
    private List<String> filters;
    private String imageUrl; // URL של התמונה (יכול להשתנות אחרי יצירת האובייקט)
    private Uri imageUri; // URI של התמונה
    private String city;
    private GeoPoint location; // מיקום המשתמש, מסוג GeoPoint
    private String id;
    ///////////////////////////////////////////////
    private Bitmap imageBitmap;
    private String imageBase64;
    ////////////////////////////////////////////////

    // Constructor
    public Post(String description, List<String> filters, String imageUrl) {
        this.description = description;
        this.filters = filters;
        this.imageUrl = imageUrl;
    }
    // Empty constructor for Firebase
    public Post() {}

    // Constructor for Parcel
    protected Post(Parcel in) {
        userId = in.readString();
        description = in.readString();
        filters = new ArrayList<>();
        in.readStringList(filters);
        imageUrl = in.readString();
        imageUri = in.readParcelable(Uri.class.getClassLoader());
        city = in.readString();
        double latitude = in.readDouble();
        double longitude = in.readDouble();
        location = new GeoPoint(latitude, longitude);
        id = in.readString();
    }

    // Parcelable Creator
    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userId);
        dest.writeString(description);
        dest.writeStringList(filters);
        dest.writeString(imageUrl);
        dest.writeParcelable(imageUri, flags);
        dest.writeString(city);
        if (location != null) {
            dest.writeDouble(location.getLatitude());
            dest.writeDouble(location.getLongitude());
        } else {
            dest.writeDouble(0.0);
            dest.writeDouble(0.0);
        }
        dest.writeString(id);
    }

    // Getters and setters
    public String getId() { //
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getFilters() {
        return filters;
    }

    public void setFilters(List<String> filters) {
        this.filters = filters;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;

    }
    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }
    // הוספת גטרים וסטטרים לשדה העיר
    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    //////////////////////////////////////////////////////////////
    public void setImageBitmap(Bitmap imageBitmap) {
        this.imageBitmap = imageBitmap;
    }

    public Bitmap getImageBitmap() {
        return this.imageBitmap;
    }

    public String getImageBase64() {
        return this.imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }
    public boolean hasFilter(String filter) {
        if (filters == null) return false;
        return filters.contains(filter);
    }
    ////////////////////////////////////////////////////////////////////
}
