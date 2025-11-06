package com.example.medibookandroid.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.medibookandroid.data.model.Patient;
import com.google.firebase.firestore.FirebaseFirestore;
// ⭐️ THÊM IMPORT NÀY (Bạn phải tự tạo file interface này)
import com.example.medibookandroid.data.repository.OnOperationCompleteListener;

/**
 * Repository for handling all data operations related to Patients.
 */
public class PatientRepository {

    private static final String TAG = "PatientRepository";
    private static final String PATIENT_COLLECTION = "patients";
    private FirebaseFirestore db;

    public PatientRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Creates a new patient profile in Firestore.
     * The document ID will be the same as the user's authentication UID.
     *
     * @param patient The patient object to create. The userId must be set.
     * @return LiveData<Boolean> indicating success (true) or failure (false).
     */
    public LiveData<Boolean> createPatient(Patient patient) {
        MutableLiveData<Boolean> successLiveData = new MutableLiveData<>();
        if (patient.getUserId() == null || patient.getUserId().isEmpty()) {
            Log.e(TAG, "User ID is missing. Cannot create patient profile.");
            successLiveData.setValue(false);
            return successLiveData;
        }
        db.collection(PATIENT_COLLECTION).document(patient.getUserId()).set(patient)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Patient profile created for user: " + patient.getUserId());
                    successLiveData.setValue(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating patient profile", e);
                    successLiveData.setValue(false);
                });
        return successLiveData;
    }

    /**
     * Fetches a single patient's profile from Firestore by their ID.
     *
     * @param patientId The UID of the patient.
     * @return LiveData containing the Patient object, or null if not found or on failure.
     */
    public LiveData<Patient> getPatientById(String patientId) {
        MutableLiveData<Patient> patientLiveData = new MutableLiveData<>();
        db.collection(PATIENT_COLLECTION).document(patientId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        patientLiveData.setValue(documentSnapshot.toObject(Patient.class));
                    } else {
                        Log.w(TAG, "Patient profile not found for ID: " + patientId);
                        patientLiveData.setValue(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting patient by ID", e);
                    patientLiveData.setValue(null);
                });
        return patientLiveData;
    }

    // ⭐️ BẮT ĐẦU SỬA ⭐️
    /**
     * Updates a patient's profile in Firestore.
     *
     * @param patient The patient object with updated information.
     * @param listener Callback to report success (true) or failure (false).
     */
    public void updatePatient(Patient patient, OnOperationCompleteListener listener) {
        // ID của document là UID của user (lưu trong userId)
        if (patient.getUserId() == null || patient.getUserId().isEmpty()) {
            Log.e(TAG, "User ID is missing. Cannot update profile.");
            listener.onComplete(false);
            return;
        }

        // Dùng patient.getUserId() làm ID document (theo logic AuthRepository)
        db.collection(PATIENT_COLLECTION).document(patient.getUserId())
                .set(patient) // Dùng .set() để ghi đè toàn bộ
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Patient profile updated successfully: " + patient.getUserId());
                    listener.onComplete(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating patient profile", e);
                    listener.onComplete(false);
                });
    }
    // ⭐️ KẾT THÚC SỬA ⭐️
}