package com.example.sharedfood.admin;

/**
 * The Admin class represents an administrator in the ShareFood application.
 * Each admin has an email and a flag indicating whether they are a super admin.
 */
public class Admin {

    // The email address of the admin (cannot be changed after initialization)
    private final String email;

    // Boolean flag indicating if the admin has super admin privileges
    private final boolean isSuperAdmin;

    /**
     * Constructor for the Admin class.
     *
     * @param email        The email address of the admin.
     * @param isSuperAdmin A boolean indicating if the admin is a super admin.
     */
    public Admin(String email, boolean isSuperAdmin) {
        this.email = email;
        this.isSuperAdmin = isSuperAdmin;
    }

    /**
     * Gets the email address of the admin.
     *
     * @return The email of the admin.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Checks if the admin is a super admin.
     *
     * @return true if the admin is a super admin, false otherwise.
     */
    public boolean isSuperAdmin() {
        return isSuperAdmin;
    }
}
