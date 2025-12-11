package com.example.medibookandroid.data.model;

import com.google.firebase.firestore.DocumentId;
import java.io.Serializable;
import com.google.firebase.firestore.IgnoreExtraProperties; // ⭐️ Import cái này

@IgnoreExtraProperties
public class Doctor implements Serializable {

    @DocumentId
    private String doctorId;

    private String userId;
    private String fullName; // Added field
    private String phone;    // Added field
    private String specialty;
    private String qualifications;
    private String workplace;
    private String about;
    private String avatarUrl;

    // ⭐️ THÊM 2 TRƯỜNG NÀY (Phải khớp tên với ReviewRepository đã update)
    private double rating = 0.0;
    private long reviewCount = 0;

    public Doctor() {
    }

    public Doctor(String doctorId, String userId, String fullName, String phone, String specialty, String qualifications, String workplace, String about, String avatarUrl, double rating, long reviewCount) {
        this.doctorId = doctorId;
        this.userId = userId;
        this.fullName = fullName;
        this.phone = phone;
        this.specialty = specialty;
        this.qualifications = qualifications;
        this.workplace = workplace;
        this.about = about;
        this.avatarUrl = avatarUrl;
        this.rating = rating;
        this.reviewCount = reviewCount;
    }
    // --- Getters and Setters ---

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
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

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getQualifications() {
        return qualifications;
    }

    public void setQualifications(String qualifications) {
        this.qualifications = qualifications;
    }

    public String getWorkplace() {
        return workplace;
    }

    public void setWorkplace(String workplace) {
        this.workplace = workplace;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public long getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(long reviewCount) {
        this.reviewCount = reviewCount;
    }
}