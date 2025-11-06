package com.example.medibookandroid.data.model;

import com.google.firebase.firestore.DocumentId;
import java.io.Serializable;

/**
 * Lớp Model đại diện cho một lịch làm việc (ca khám) của bác sĩ.
 * Khớp với cấu trúc collection 'doctor_schedules' trên Firestore.
 */
public class DoctorSchedule implements Serializable {

    @DocumentId // Firestore sẽ tự động map Document ID (ví dụ: "autoId_1") vào đây
    private String scheduleId;

    private String doctorId;    // "uid_xyz789"
    private String date;        // "2025-11-03"
    private String startTime;   // "08:00"
    private String endTime;     // "10:00"
    private boolean available; // true

    // Constructor rỗng bắt buộc cho Firestore
    public DoctorSchedule() {}

    /**
     * Constructor chính để tạo một lịch làm việc mới
     */
    public DoctorSchedule(String doctorId, String date, String startTime, String endTime, boolean isAvailable) {
        this.doctorId = doctorId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.available = isAvailable;;
    }

    // --- Getters and Setters ---
    // (Bắt buộc cho Firestore)

    public String getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}