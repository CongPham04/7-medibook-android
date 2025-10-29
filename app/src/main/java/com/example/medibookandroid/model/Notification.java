package com.example.medibookandroid.model;

import java.util.Date;
import java.util.UUID;

/**
 * Lớp Model đại diện cho một thông báo
 */
public class Notification {
    private String id;
    private int patientId;
    private String title;
    private String body;
    private Date date;
    private boolean isRead;

    // Constructor trống cho Gson
    public Notification() {}

    public Notification(int patientId, String title, String body) {
        this.id = UUID.randomUUID().toString();
        this.patientId = patientId;
        this.title = title;
        this.body = body;
        this.date = new Date(); // Ngày giờ hiện tại
        this.isRead = false;
    }

    // Getters (Gson cần chúng)
    public String getId() { return id; }
    public int getPatientId() { return patientId; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public Date getDate() { return date; }
    public boolean isRead() { return isRead; }

    public void setRead(boolean read) { isRead = read; }
}
