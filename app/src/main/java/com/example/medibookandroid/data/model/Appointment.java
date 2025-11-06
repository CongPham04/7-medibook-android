package com.example.medibookandroid.data.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;

/**
 * Lớp Model đại diện cho một Lịch hẹn (Appointment)
 * Đã cập nhật để khớp với collection 'appointments' trên Firestore.
 */
public class Appointment implements Serializable {

    @DocumentId // Firestore sẽ tự động map Document ID (ví dụ: "autoId_2") vào đây
    private String appointmentId;

    private String patientId;    // "uid_abc123" (thay vì int)
    private String doctorId;     // "uid_xyz789" (thay vì int)
    private String scheduleId;   // "autoId_1"
    private String date;         // "2025-11-03"
    private String time;         // "09:00"
    private String description;  // "Đau ngực..." (thay cho 'symptoms')
    private String status;       // "pending", "confirmed", "cancelled", "completed"

    @ServerTimestamp // Tự động gán ngày giờ server khi tạo
    private Date createdAt;

    @ServerTimestamp // Tự động gán/cập nhật ngày giờ server khi ghi
    private Date updatedAt;

    // Constructor rỗng bắt buộc cho Firestore
    public Appointment() {}

    /**
     * Constructor để tạo một lịch hẹn mới (chưa gửi lên server)
     */
    public Appointment(String patientId, String doctorId, String scheduleId, String date, String time, String description, String status) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.scheduleId = scheduleId;
        this.date = date;
        this.time = time;
        this.description = description;
        this.status = status;
        // appointmentId, createdAt, updatedAt sẽ do Firestore quản lý
    }

    // --- Getters and Setters ---
    // (Bắt buộc cho Firestore)

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