package com.example.medibookandroid.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.medibookandroid.data.model.Appointment;
import com.example.medibookandroid.data.model.Patient;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository for handling all data operations related to Appointments.
 */
public class AppointmentRepository {

    private static final String TAG = "AppointmentRepository";
    private static final String APPOINTMENT_COLLECTION = "appointments";
    private FirebaseFirestore db;

    public AppointmentRepository() {
        // Initialize Firestore instance
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Tạo lịch hẹn mới VÀ cập nhật ca làm việc thành "đã đặt" (atomic).
     */
    public void createAppointment(Appointment appointment, OnOperationCompleteListener listener) {
        // 1. Lấy WriteBatch
        WriteBatch batch = db.batch();

        // 2. Tạo document mới cho 'appointments'
        DocumentReference newAppointmentRef = db.collection(APPOINTMENT_COLLECTION).document();
        batch.set(newAppointmentRef, appointment);

        // 3. Cập nhật 'doctor_schedules'
        String scheduleId = appointment.getScheduleId();
        if (scheduleId == null || scheduleId.isEmpty()) {
            Log.e(TAG, "Schedule ID is missing! Cannot update schedule availability.");
            listener.onComplete(false); // Báo lỗi
            return;
        }

        DocumentReference scheduleRef = db.collection("doctor_schedules").document(scheduleId);
        // QUAN TRỌNG: Đảm bảo tên trường này đúng (available)
        batch.update(scheduleRef, "available", false);

        // 4. Thực thi
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Appointment created AND schedule updated successfully.");
                    listener.onComplete(true); // Báo thành công
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error in batch write for creating appointment", e);
                    listener.onComplete(false); // Báo thất bại
                });
    }

    /**
     * Fetches all appointments for a specific patient.
     */
    public LiveData<List<Appointment>> getAppointmentsForPatient(String patientId, MutableLiveData<Boolean> loadingLiveData) {
        loadingLiveData.setValue(true); // Bật loading
        MutableLiveData<List<Appointment>> appointmentsLiveData = new MutableLiveData<>();
        db.collection(APPOINTMENT_COLLECTION)
                .whereEqualTo("patientId", patientId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Appointment> appointments = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        appointments.add(document.toObject(Appointment.class));
                    }
                    appointmentsLiveData.setValue(appointments);
                    loadingLiveData.setValue(false); // Tắt loading
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting appointments for patient", e);
                    appointmentsLiveData.setValue(new ArrayList<>()); // Trả list rỗng
                    loadingLiveData.setValue(false); // Tắt loading (khi lỗi)
                });
        return appointmentsLiveData;
    }

    /**
     * Fetches all appointments for a specific doctor.
     */
    public LiveData<List<Appointment>> getAppointmentsForDoctor(String doctorId) {
        MutableLiveData<List<Appointment>> appointmentsLiveData = new MutableLiveData<>();
        db.collection(APPOINTMENT_COLLECTION)
                .whereEqualTo("doctorId", doctorId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Appointment> appointments = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        appointments.add(document.toObject(Appointment.class));
                    }
                    appointmentsLiveData.setValue(appointments);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting appointments for doctor", e);
                    appointmentsLiveData.setValue(null);
                });
        return appointmentsLiveData;
    }


    /**
     * Updates ONLY the status of an appointment.
     * (Dùng cho "Accept" hoặc "Complete")
     */
    public void updateAppointmentStatus(String appointmentId, String status, OnOperationCompleteListener listener) {
        db.collection(APPOINTMENT_COLLECTION).document(appointmentId)
                .update("status", status)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Appointment status updated successfully: " + appointmentId);
                    listener.onComplete(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating appointment status", e);
                    listener.onComplete(false);
                });
    }

    /**
     * Lắng nghe (real-time) TẤT CẢ các lịch hẹn đang "pending" cho một bác sĩ
     */
    public void listenForPendingAppointments(String doctorId, MutableLiveData<List<Appointment>> appointmentsLiveData, MutableLiveData<Boolean> loadingLiveData) {
        loadingLiveData.setValue(true); // Bật loading
        db.collection(APPOINTMENT_COLLECTION)
                .whereEqualTo("doctorId", doctorId)
                .whereEqualTo("status", "pending")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error listening for pending appointments", e);
                        appointmentsLiveData.setValue(new ArrayList<>());
                        loadingLiveData.setValue(false);
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        List<Appointment> appointments = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            appointments.add(document.toObject(Appointment.class));
                        }
                        appointmentsLiveData.setValue(appointments);
                    }
                    loadingLiveData.setValue(false); // Tắt loading
                });
    }

    // ⭐️ BẮT ĐẦU SỬA: Thêm 'loadingLiveData' ⭐️
    /**
     * Lấy các lịch hẹn ĐÃ XÁC NHẬN cho bác sĩ theo NGÀY CỤ THỂ
     */
    public void getConfirmedAppointmentsForDoctorByDate(String doctorId, String date, MutableLiveData<List<Appointment>> appointmentsLiveData, MutableLiveData<Boolean> loadingLiveData) {
        loadingLiveData.setValue(true); // Bật loading

        db.collection(APPOINTMENT_COLLECTION)
                .whereEqualTo("doctorId", doctorId)
                .whereEqualTo("date", date)
                .whereEqualTo("status", "confirmed")
                // (Truy vấn này có thể cần Index, hãy kiểm tra Logcat)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Appointment> appointments = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        appointments.add(document.toObject(Appointment.class));
                    }
                    appointmentsLiveData.setValue(appointments);
                    loadingLiveData.setValue(false); // Tắt loading
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting confirmed appointments by date", e);
                    appointmentsLiveData.setValue(new ArrayList<>());
                    loadingLiveData.setValue(false); // Tắt loading
                });
    }
    // ⭐️ KẾT THÚC SỬA ⭐️

    /**
     * Lấy thông tin chi tiết của 1 Patient (Dùng cho Adapter)
     */
    public void getPatientInfo(String patientId, MutableLiveData<Patient> patientLiveData) {
        db.collection("patients").document(patientId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        patientLiveData.setValue(documentSnapshot.toObject(Patient.class));
                    } else {
                        patientLiveData.setValue(null); // Không tìm thấy
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting patient info: ", e);
                    patientLiveData.setValue(null);
                });
    }

    /**
     * Hủy một lịch hẹn VÀ mở lại ca làm việc (DoctorSchedule)
     * (Dùng cho cả Bác sĩ từ chối và Bệnh nhân tự hủy)
     */
    public void cancelAppointmentAndFreeSlot(Appointment appointment, OnOperationCompleteListener listener) {
        // 1. Lấy WriteBatch
        WriteBatch batch = db.batch();

        // 2. Cập nhật 'appointments' -> status: "cancelled"
        DocumentReference apptRef = db.collection(APPOINTMENT_COLLECTION).document(appointment.getAppointmentId());
        batch.update(apptRef, "status", "cancelled");

        // 3. Cập nhật 'doctor_schedules' -> available: true
        String scheduleId = appointment.getScheduleId();
        if (scheduleId != null && !scheduleId.isEmpty()) {
            DocumentReference scheduleRef = db.collection("doctor_schedules").document(scheduleId);
            batch.update(scheduleRef, "available", true); // Mở lại ca
        } else {
            Log.w(TAG, "Không thể mở lại ca: Thiếu Schedule ID trong Lịch hẹn " + appointment.getAppointmentId());
        }

        // 4. Thực thi
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Đã hủy lịch hẹn và mở lại ca thành công.");
                    listener.onComplete(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi hủy lịch hẹn (batch write)", e);
                    listener.onComplete(false);
                });
    }
}