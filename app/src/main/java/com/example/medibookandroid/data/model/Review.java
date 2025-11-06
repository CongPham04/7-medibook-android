package com.example.medibookandroid.data.model; // ⭐️ Sửa package nếu cần

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

/**
 * Model (POJO) cho một document trong collection 'reviews'.
 */
public class Review {

    @DocumentId
    private String id; // Sẽ tự động gán ID của document (ví dụ: autoId_4)

    private String doctorId;
    private String patientId;
    private String patientName; // ⭐️ Thêm vào để ReviewAdapter hoạt động
    private float rating;       // Dùng float để hỗ trợ 4.5 sao
    private String comment;

    @ServerTimestamp
    private Date createdAt; // Dùng Date và @ServerTimestamp để Firestore tự gán giờ

    // --- Constructors ---

    // Constructor rỗng (BẮT BUỘC cho Firestore)
    public Review() {
    }

    // Constructor tiện ích (khi TẠO MỚI review)
    public Review(String doctorId, String patientId, String patientName, float rating, String comment) {
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.patientName = patientName;
        this.rating = rating;
        this.comment = comment;
        // createdAt sẽ được gán tự động bởi server
    }

    // --- Getters and Setters ---
    // (BẮT BUỘC cho Firestore để đọc/ghi dữ liệu)

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}