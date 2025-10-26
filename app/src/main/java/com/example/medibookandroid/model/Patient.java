package com.example.medibookandroid.model;

import java.io.Serializable;

public class Patient implements Serializable {
    private int id;
    private String fullName;
    private String dob;
    private String gender;
    private String phone;
    private String address;

    public Patient(int id, String fullName, String dob, String gender, String phone, String address) {
        this.id = id;
        this.fullName = fullName;
        this.dob = dob;
        this.gender = gender;
        this.phone = phone;
        this.address = address;
    }

    public int getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getDob() {
        return dob;
    }

    public String getGender() {
        return gender;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }
}
