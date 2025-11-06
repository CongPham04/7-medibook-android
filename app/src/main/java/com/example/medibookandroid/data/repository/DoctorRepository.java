package com.example.medibookandroid.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.medibookandroid.data.model.Doctor;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository for handling all data operations related to Doctors.
 */
public class DoctorRepository {

    private static final String TAG = "DoctorRepository";
    private static final String DOCTOR_COLLECTION = "doctors";
    private FirebaseFirestore db;

    public DoctorRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Creates a new doctor profile in Firestore.
     * The document ID will be the same as the user's authentication UID.
     *
     * @param doctor The doctor object to create. The userId must be set.
     * @return LiveData<Boolean> indicating success (true) or failure (false).
     */
    public LiveData<Boolean> createDoctor(Doctor doctor) {
        MutableLiveData<Boolean> successLiveData = new MutableLiveData<>();
        if (doctor.getUserId() == null || doctor.getUserId().isEmpty()) {
            Log.e(TAG, "User ID is missing. Cannot create doctor profile.");
            successLiveData.setValue(false);
            return successLiveData;
        }
        db.collection(DOCTOR_COLLECTION).document(doctor.getUserId()).set(doctor)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Doctor profile created for user: " + doctor.getUserId());
                    successLiveData.setValue(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating doctor profile", e);
                    successLiveData.setValue(false);
                });
        return successLiveData;
    }

    /**
     * Fetches a list of all doctors from Firestore.
     *
     * @return LiveData containing the list of doctors, or null on failure.
     */
    public LiveData<List<Doctor>> getAllDoctors() {
        MutableLiveData<List<Doctor>> doctorsLiveData = new MutableLiveData<>();
        db.collection(DOCTOR_COLLECTION).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Doctor> doctors = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        doctors.add(document.toObject(Doctor.class));
                    }
                    doctorsLiveData.setValue(doctors);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting all doctors", e);
                    doctorsLiveData.setValue(null);
                });
        return doctorsLiveData;
    }

    /**
     * Fetches a single doctor's profile from Firestore by their ID.
     *
     * @param doctorId The UID of the doctor.
     * @return LiveData containing the Doctor object, or null if not found or on failure.
     */
    public LiveData<Doctor> getDoctorById(String doctorId) {
        MutableLiveData<Doctor> doctorLiveData = new MutableLiveData<>();
        db.collection(DOCTOR_COLLECTION).document(doctorId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        doctorLiveData.setValue(documentSnapshot.toObject(Doctor.class));
                    } else {
                        Log.w(TAG, "Doctor profile not found for ID: " + doctorId);
                        doctorLiveData.setValue(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting doctor by ID", e);
                    doctorLiveData.setValue(null);
                });
        return doctorLiveData;
    }

    // ⭐️ BẮT ĐẦU SỬA ⭐️
    /**
     * Updates a doctor's profile in Firestore.
     *
     * @param doctor The doctor object with updated information.
     * @param listener Callback to report success (true) or failure (false).
     */
    public void updateDoctor(Doctor doctor, OnOperationCompleteListener listener) {
        // Dùng doctorId (là UID) làm ID document
        if (doctor.getDoctorId() == null || doctor.getDoctorId().isEmpty()) {
            Log.e(TAG, "Doctor ID is missing. Cannot update profile.");
            listener.onComplete(false);
            return;
        }
        db.collection(DOCTOR_COLLECTION).document(doctor.getDoctorId())
                .set(doctor) // .set() để ghi đè
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Doctor profile updated successfully: " + doctor.getDoctorId());
                    listener.onComplete(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating doctor profile", e);
                    listener.onComplete(false);
                });
    }
    // ⭐️ KẾT THÚC SỬA ⭐️

    /**
     * Searches for doctors by name or specialty.
     *
     * @param query The search query.
     * @return LiveData containing the list of matching doctors, or null on failure.
     */
    public LiveData<List<Doctor>> searchDoctors(String query) {
        MutableLiveData<List<Doctor>> doctorsLiveData = new MutableLiveData<>();
        if (query == null || query.isEmpty()) {
            return getAllDoctors();
        }
        // This is a simple search. For more complex queries, you might need a more advanced solution like Algolia or Elasticsearch.
        db.collection(DOCTOR_COLLECTION)
                .orderBy("fullName")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Doctor> doctors = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        doctors.add(document.toObject(Doctor.class));
                    }
                    // Also search by specialty
                    db.collection(DOCTOR_COLLECTION)
                            .orderBy("specialty")
                            .startAt(query)
                            .endAt(query + "\uf8ff")
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots2 -> {
                                for (QueryDocumentSnapshot document : queryDocumentSnapshots2) {
                                    Doctor doctor = document.toObject(Doctor.class);
                                    if (!doctors.contains(doctor)) {
                                        doctors.add(doctor);
                                    }
                                }
                                doctorsLiveData.setValue(doctors);
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error searching doctors by specialty", e);
                                doctorsLiveData.setValue(null);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error searching doctors by name", e);
                    doctorsLiveData.setValue(null);
                });
        return doctorsLiveData;
    }
}