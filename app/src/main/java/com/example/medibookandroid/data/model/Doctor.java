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

    public Doctor() {}

    public Doctor(String userId, String fullName, String phone, String specialty, String qualifications, String workplace, String about, String avatarUrl) {
        this.userId = userId;
        this.fullName = fullName;
        this.phone = phone;
        this.specialty = specialty;
        this.qualifications = qualifications;
        this.workplace = workplace;
        this.about = about;
        this.avatarUrl = avatarUrl;
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
}