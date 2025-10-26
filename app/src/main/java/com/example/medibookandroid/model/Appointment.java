package com.example.medibookandroid.model;

import java.io.Serializable;
import java.util.Date;

public class Appointment implements Serializable {
    private int id;
    private int patientId;
    private int doctorId;
    private Date appointmentDate;
    private String status; // Pending, Confirmed, Canceled
    private String symptoms;

    public Appointment(int id, int patientId, int doctorId, Date appointmentDate, String status, String symptoms) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentDate = appointmentDate;
        this.status = status;
        this.symptoms = symptoms;
    }

    public int getId() {
        return id;
    }

    public int getPatientId() {
        return patientId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public Date getAppointmentDate() {
        return appointmentDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSymptoms() {
        return symptoms;
    }
}
