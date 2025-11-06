package com.example.medibookandroid.data.model;
// ... (imports)

import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;

public class User implements Serializable {
    @DocumentId
    private String uid;
    private String email; // Dùng để đăng nhập
    private String role;  // "patient" hoặc "doctor"

    public User() {}

    // Constructor mới chỉ cần email và role
    public User(String email, String role) {
        this.email = email;
        this.role = role;
    }

    // --- Getters and Setters ---
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    // ⭐️ ĐÃ XÓA fullName và phone ⭐️
}