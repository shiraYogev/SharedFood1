package com.example.sharedfood.admin;

public class Admin {
    private final String email;
    private final boolean isSuperAdmin;

    public Admin(String email, boolean isSuperAdmin) {
        this.email = email;
        this.isSuperAdmin = isSuperAdmin;
    }

    public String getEmail() {
        return email;
    }

    public boolean isSuperAdmin() {
        return isSuperAdmin;
    }
}
