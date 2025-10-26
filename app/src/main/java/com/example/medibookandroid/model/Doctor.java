package com.example.medibookandroid.model;

import java.io.Serializable;

public class Doctor implements Serializable {
    private int id;
    private String name;
    private String specialty;
    private String qualifications;
    private String workplace;
    private String phone;
    private String description;
    private double rating;

    public Doctor(int id, String name, String specialty, String qualifications, String workplace, String phone, String description, double rating) {
        this.id = id;
        this.name = name;
        this.specialty = specialty;
        this.qualifications = qualifications;
        this.workplace = workplace;
        this.phone = phone;
        this.description = description;
        this.rating = rating;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSpecialty() {
        return specialty;
    }

    public String getQualifications() {
        return qualifications;
    }

    public String getWorkplace() {
        return workplace;
    }

    public String getPhone() {
        return phone;
    }

    public String getDescription() {
        return description;
    }

    public double getRating() {
        return rating;
    }
}
