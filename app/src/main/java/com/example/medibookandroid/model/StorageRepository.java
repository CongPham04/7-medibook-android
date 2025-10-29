package com.example.medibookandroid.model;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class StorageRepository {

    private static final String PREFS_NAME = "MedibookPrefs";
    private static final String KEY_DOCTORS = "doctors";
    private static final String KEY_APPOINTMENTS = "appointments";
    private static final String KEY_LOGGED_IN_ROLE = "loggedInRole";
    // --- THÊM MỚI (BẮT ĐẦU) ---
    private static final String KEY_NOTIFICATIONS = "notifications";
    // --- THÊM MỚI (KẾT THÚC) ---

    private static StorageRepository instance = null;
    private final SharedPreferences prefs;
    private final Gson gson;

    public List<Doctor> doctors;
    public List<Appointment> appointments;
    public String loggedInRole;
    // --- THÊM MỚI (BẮT ĐẦU) ---
    public List<Notification> notifications;
    // --- THÊM MỚI (KẾT THÚC) ---


    private StorageRepository(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        loadData();
    }

    public static synchronized StorageRepository getInstance(Context context) {
        if (instance == null) {
            instance = new StorageRepository(context.getApplicationContext());
        }
        return instance;
    }

    private void loadData() {
        loggedInRole = prefs.getString(KEY_LOGGED_IN_ROLE, null);

        // Load doctors
        String doctorsJson = prefs.getString(KEY_DOCTORS, null);
        if (doctorsJson == null) {
            doctors = createMockDoctors();
            saveDoctors();
        } else {
            Type doctorListType = new TypeToken<ArrayList<Doctor>>(){}.getType();
            doctors = gson.fromJson(doctorsJson, doctorListType);
        }

        // Load appointments
        String appointmentsJson = prefs.getString(KEY_APPOINTMENTS, null);
        if (appointmentsJson == null) {
            appointments = new ArrayList<>();
        } else {
            Type appointmentListType = new TypeToken<ArrayList<Appointment>>(){}.getType();
            appointments = gson.fromJson(appointmentsJson, appointmentListType);
        }

        // --- THÊM MỚI (BẮT ĐẦU) ---
        // Load notifications
        String notificationsJson = prefs.getString(KEY_NOTIFICATIONS, null);
        if (notificationsJson == null) {
            notifications = new ArrayList<>();
        } else {
            Type notificationListType = new TypeToken<ArrayList<Notification>>(){}.getType();
            notifications = gson.fromJson(notificationsJson, notificationListType);
        }
        // --- THÊM MỚI (KẾT THÚC) ---
    }

    private List<Doctor> createMockDoctors() {
        List<Doctor> mockDoctors = new ArrayList<>();
        mockDoctors.add(new Doctor(1, "Dr. Olivia Turner", "Cardiology", "MD, FACC", "Central Clinic, City", "111-222-3333", "Expert in cardiovascular diseases.", 4.8));
        mockDoctors.add(new Doctor(2, "Dr. Ben Carter", "Neurology", "MD, PhD", "NeuroCare Institute", "222-333-4444", "Specializes in brain and nervous system disorders.", 4.9));
        mockDoctors.add(new Doctor(3, "Dr. Isabella Rossi", "Pediatrics", "MD, FAAP", "Children's Health Center", "333-444-5555", "Dedicated to child and adolescent health.", 4.7));
        return mockDoctors;
    }

    private void saveDoctors() {
        String doctorsJson = gson.toJson(doctors);
        prefs.edit().putString(KEY_DOCTORS, doctorsJson).apply();
    }

    public void saveAppointments() {
        String appointmentsJson = gson.toJson(appointments);
        prefs.edit().putString(KEY_APPOINTMENTS, appointmentsJson).apply();
    }

    // --- THÊM MỚI (BẮT ĐẦU) ---
    public void saveNotifications() {
        String notificationsJson = gson.toJson(notifications);
        prefs.edit().putString(KEY_NOTIFICATIONS, notificationsJson).apply();
    }
    // --- THÊM MỚI (KẾT THÚC) ---

    public void loginUser(String role) {
        loggedInRole = role;
        prefs.edit().putString(KEY_LOGGED_IN_ROLE, role).apply();
    }

    public void logoutUser() {
        loggedInRole = null;
        prefs.edit().remove(KEY_LOGGED_IN_ROLE).apply();
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
