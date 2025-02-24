package com.example.sharedfood.user;

/**
 * Represents a user in the SharedFood application.
 * This class holds basic user information including the user's email address,
 * whether the user is permanently banned, and the temporary ban time if applicable.
 */
public class User {
    // User's email address
    private String email;

    // Flag indicating whether the user is permanently banned
    private boolean isBanned;

    // Temporary ban time for the user (e.g., a timestamp in milliseconds)
    // If the user is not temporarily banned, this field is null
    private Long tempBanTime;

    /**
     * Constructor with three parameters.
     *
     * @param email       The email address of the user.
     * @param isBanned    A boolean flag indicating if the user is banned.
     * @param tempBanTime The temporary ban time for the user (can be null if not applicable).
     */
    public User(String email, boolean isBanned, Long tempBanTime) {
        this.email = email;
        this.isBanned = isBanned;
        this.tempBanTime = tempBanTime;
    }

    /**
     * Constructor with two parameters.
     * This constructor sets the temporary ban time to null by default.
     *
     * @param email    The email address of the user.
     * @param isBanned A boolean flag indicating if the user is banned.
     */
    public User(String email, boolean isBanned) {
        this.email = email;
        this.isBanned = isBanned;
        this.tempBanTime = null; // Default to null since no temporary ban time is provided
    }

    /**
     * Retrieves the user's email address.
     *
     * @return The email address of the user.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email address.
     *
     * @param email The email address to set.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Checks if the user is banned.
     *
     * @return True if the user is banned, false otherwise.
     */
    public boolean isBanned() {
        return isBanned;
    }

    /**
     * Sets the ban status of the user.
     *
     * @param banned True to mark the user as banned, false otherwise.
     */
    public void setBanned(boolean banned) {
        isBanned = banned;
    }

    /**
     * Retrieves the temporary ban time of the user.
     *
     * @return The temporary ban time, or null if not set.
     */
    public Long getTempBanTime() {
        return tempBanTime;
    }

    /**
     * Sets the temporary ban time for the user.
     *
     * @param tempBanTime The temporary ban time to set.
     */
    public void setTempBanTime(Long tempBanTime) {
        this.tempBanTime = tempBanTime;
    }
}
