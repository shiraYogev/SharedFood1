package com.example.sharedfood.post;

import android.graphics.Bitmap;
import android.net.Uri;
import java.util.ArrayList;
import java.util.List;
import com.google.firebase.firestore.GeoPoint;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Represents a post in the ShareFood application.
 * Implements Parcelable to allow passing objects between Android components.
 */
public class Post implements Parcelable {
    private String userId; // ID of the user who created the post
    private String description; // Text description of the post
    private List<String> filters; // List of filters/tags for categorization
    private String imageUrl; // URL of the uploaded image
    private Uri imageUri; // URI of the image, used for local storage reference
    private String city; // City where the post is relevant
    private GeoPoint location; // Geographical location (latitude, longitude)
    private String id; // Unique identifier of the post in the database
    private Bitmap imageBitmap; // Bitmap representation of the image for display
    private String imageBase64; // Base64 encoded image data for transfer

    /**
     * Constructor with main attributes.
     * Used when creating a new post manually.
     */
    public Post(String description, List<String> filters, String imageUrl) {
        this.description = description;
        this.filters = filters;
        this.imageUrl = imageUrl;
    }

    /**
     * Default constructor required for Firebase Firestore.
     */
    public Post() {}

    /**
     * Constructor for reading from Parcel (used for Parcelable implementation).
     */
    protected Post(Parcel in) {
        userId = in.readString();
        description = in.readString();
        filters = new ArrayList<>();
        in.readStringList(filters);
        imageUrl = in.readString();
        imageUri = in.readParcelable(Uri.class.getClassLoader());
        city = in.readString();

        // Reading location as latitude and longitude values
        double latitude = in.readDouble();
        double longitude = in.readDouble();
        location = new GeoPoint(latitude, longitude);

        id = in.readString();
    }

    /**
     * Parcelable creator for creating Post objects from a Parcel.
     */
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

    /**
     * Writes the object's data into a Parcel for transfer.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userId);
        dest.writeString(description);
        dest.writeStringList(filters);
        dest.writeString(imageUrl);
        dest.writeParcelable(imageUri, flags);
        dest.writeString(city);

        // If location exists, write coordinates; otherwise, write default values
        if (location != null) {
            dest.writeDouble(location.getLatitude());
            dest.writeDouble(location.getLongitude());
        } else {
            dest.writeDouble(0.0);
            dest.writeDouble(0.0);
        }
        dest.writeString(id);
    }

    // =================== Getters and Setters ===================

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getFilters() { return filters; }
    public void setFilters(List<String> filters) { this.filters = filters; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Uri getImageUri() { return imageUri; }
    public void setImageUri(Uri imageUri) { this.imageUri = imageUri; }

    public GeoPoint getLocation() { return location; }
    public void setLocation(GeoPoint location) { this.location = location; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public void setImageBitmap(Bitmap imageBitmap) { this.imageBitmap = imageBitmap; }
    public Bitmap getImageBitmap() { return this.imageBitmap; }

    public String getImageBase64() { return this.imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }

    /**
     * Checks if the post contains a specific filter.
     *
     * @param filter The filter to check.
     * @return True if the filter exists, false otherwise.
     */
    public boolean hasFilter(String filter) {
        if (filters == null) return false;
        return filters.contains(filter);
    }

    /**
     * Placeholder method for setting a timestamp.
     * Currently not implemented.
     *
     * @param l Timestamp value.
     * @return Always returns 0.
     */
    public int setTimestamp(long l) { return 0; }
}
