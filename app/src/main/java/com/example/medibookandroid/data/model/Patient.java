package com.example.medibookandroid.data.model;

import com.google.firebase.firestore.DocumentId;
import java.io.Serializable;
import com.google.firebase.firestore.IgnoreExtraProperties; // ⭐️ Import cái này

@IgnoreExtraProperties
public class Patient implements Serializable {

    @DocumentId
    private String patientId; // Sẽ được gán = uid khi đọc

    private String userId;    // uid (lưu 2 lần, 1 ở ID, 1 ở field)
    private String fullName;
    private String phone;
    private String gender;
    private String dob;       // yyyy-MM-dd
    private String address;
    private String avatarUrl;

    public Patient() {}

    // Constructor chúng ta sẽ dùng
    public Patient(String userId, String fullName, String phone, String gender, String dob, String address, String avatarUrl) {
        this.userId = userId;
        this.fullName = fullName;
        this.phone = phone;
        this.gender = gender;
        this.dob = dob;
        this.address = address;
        this.avatarUrl = avatarUrl;
    }

    // --- Getters and Setters ---

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    // Added Getter/Setter
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    // Added Getter/Setter
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}