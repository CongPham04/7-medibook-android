package com.example.medibookandroid.data.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;

public class Appointment implements Serializable {

    @DocumentId
    private String appointmentId;

    private String patientId;
    private String doctorId;
    private String scheduleId;
    private String date;
    private String time;
    private String description;
    private String status;

    // Trường mới thêm cho chức năng đánh giá
    private boolean isReviewed = false;

    @ServerTimestamp
    private Date createdAt;
    @ServerTimestamp
    private Date updatedAt;

    // --- CONSTRUCTORS ---

    // 1. Constructor rỗng (BẮT BUỘC cho Firestore)
    public Appointment() {}

    // 2. ⭐️ Constructor dùng để TẠO MỚI lịch hẹn (Sửa lỗi của bạn tại đây)
    // Nhận 7 tham số từ BookingSummaryFragment
    public Appointment(String patientId, String doctorId, String scheduleId, String date, String time, String description, String status) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.scheduleId = scheduleId;
        this.date = date;
        this.time = time;
        this.description = description;
        this.status = status;

        // Mặc định khi tạo mới là chưa đánh giá
        this.isReviewed = false;
    }

    // --- GETTERS AND SETTERS ---

    public String getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @PropertyName("isReviewed")
    public boolean isReviewed() {
        return isReviewed;
    }

    @PropertyName("isReviewed")
    public void setReviewed(boolean reviewed) {
        isReviewed = reviewed;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}