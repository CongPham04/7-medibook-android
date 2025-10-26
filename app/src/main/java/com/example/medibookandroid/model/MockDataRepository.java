package com.example.medibookandroid.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MockDataRepository {

    private static MockDataRepository instance = null;

    public final List<Doctor> doctors;
    public final List<Appointment> appointments;
    public Patient currentPatient;
    public Doctor currentDoctor;

    private MockDataRepository() {
        doctors = new ArrayList<>();
        appointments = new ArrayList<>();
        populateDoctors();
        populateAppointments();
        // Setup a default patient for login
        currentPatient = new Patient(1, "Jane Doe", "01/01/1990", "Female", "0900000000", "123 Main St");
    }

    public static synchronized MockDataRepository getInstance() {
        if (instance == null) {
            instance = new MockDataRepository();
        }
        return instance;
    }

    private void populateDoctors() {
        doctors.add(new Doctor(1, "Dr. Olivia Turner", "Cardiology", "MD, FACC", "Central Clinic, City", "111-222-3333", "Expert in cardiovascular diseases.", 4.8));
        doctors.add(new Doctor(2, "Dr. Ben Carter", "Neurology", "MD, PhD", "NeuroCare Institute", "222-333-4444", "Specializes in brain and nervous system disorders.", 4.9));
        doctors.add(new Doctor(3, "Dr. Isabella Rossi", "Pediatrics", "MD, FAAP", "Children's Health Center", "333-444-5555", "Dedicated to child and adolescent health.", 4.7));
    }

    private void populateAppointments() {
        Calendar calendar = Calendar.getInstance();

        // Pending appointment
        calendar.set(2025, Calendar.OCTOBER, 28, 10, 0);
        Date pendingDate = calendar.getTime();
        appointments.add(new Appointment(1, 1, 1, pendingDate, "Pending", "Chest pain and shortness of breath."));

        // Confirmed appointment
        calendar.set(2025, Calendar.OCTOBER, 29, 14, 30);
        Date confirmedDate = calendar.getTime();
        appointments.add(new Appointment(2, 1, 2, confirmedDate, "Confirmed", "Migraine headaches."));
    }

    public Doctor findDoctorById(int id) {
        for (Doctor doctor : doctors) {
            if (doctor.getId() == id) {
                return doctor;
            }
        }
        return null;
    }
}
