package com.example.medibookandroid.data.repository;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.medibookandroid.data.model.Appointment;
import com.example.medibookandroid.data.model.Patient;
import com.example.medibookandroid.data.remote.FCMRequest;
import com.example.medibookandroid.data.remote.FCMRequestV1;
import com.example.medibookandroid.data.remote.FCMResponse;
import com.example.medibookandroid.data.remote.RetrofitClient;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// ⭐️ IMPORT ĐÚNG CHO RETROFIT
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository for handling all data operations related to Appointments.
 */
public class AppointmentRepository {

    private static final String TAG = "AppointmentRepository";
    private static final String APPOINTMENT_COLLECTION = "appointments";
    private FirebaseFirestore db;

    public AppointmentRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Lấy danh sách lịch hẹn của Bệnh nhân
     */
    public LiveData<List<Appointment>> getAppointmentsForPatient(String patientId, MutableLiveData<Boolean> loadingLiveData) {
        loadingLiveData.setValue(true);
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
                    loadingLiveData.setValue(false);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting appointments for patient", e);
                    appointmentsLiveData.setValue(new ArrayList<>());
                    loadingLiveData.setValue(false);
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

    // --- PHẦN 1: BỆNH NHÂN ĐẶT LỊCH -> BÁO BÁC SĨ ---

    public void createAppointment(Appointment appointment, OnOperationCompleteListener listener) {
        db.collection("appointments").add(appointment)
                .addOnSuccessListener(docRef -> {
                    // 1. Lưu DB thành công
                    // 2. Lấy Token của Bác sĩ để gửi thông báo
                    notifyDoctor(appointment.getDoctorId(), "📅 Có lịch hẹn mới!", "Bệnh nhân vừa đặt lịch khám.");

                    listener.onComplete(true);
                })
                .addOnFailureListener(e -> listener.onComplete(false));
    }

    private void notifyDoctor(String doctorId, String title, String body) {
        db.collection("doctors").document(doctorId).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        String token = snapshot.getString("fcmToken");
                        if (token != null) {
                            sendFCM(token, title, body);
                        }
                    }
                });
    }

    // --- PHẦN 2: BÁC SĨ XÁC NHẬN/HỦY -> BÁO BỆNH NHÂN ---

    public void updateAppointmentStatus(String appointmentId, String newStatus, String patientId, OnOperationCompleteListener listener) {
        db.collection("appointments").document(appointmentId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    // 1. Update DB thành công
                    listener.onComplete(true);

                    // 2. Gửi thông báo cho bệnh nhân
                    String title = "";
                    String body = "";

                    if (newStatus.equals("confirmed")) {
                        title = "✅ Lịch hẹn được xác nhận";
                        body = "Bác sĩ đã đồng ý lịch khám của bạn.";
                    } else if (newStatus.equals("cancelled")) {
                        title = "❌ Lịch hẹn bị hủy";
                        body = "Bác sĩ đã hủy lịch khám của bạn.";
                    }

                    if (!title.isEmpty()) {
                        notifyPatient(patientId, title, body);
                    }
                })
                .addOnFailureListener(e -> listener.onComplete(false));
    }

    private void notifyPatient(String patientId, String title, String body) {
        db.collection("patients").document(patientId).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        String token = snapshot.getString("fcmToken");
                        if (token != null) {
                            sendFCM(token, title, body);
                        }
                    }
                });
    }

    // --- HÀM GỬI CHUNG (Dùng Retrofit) ---
    private void sendFCM(String token, String title, String body) {
        // 1. Chuẩn bị dữ liệu
        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("type", "booking_update");
        // Lưu ý: data trong V1 tất cả value phải là String

        // 2. Tạo Request theo cấu trúc V1
        FCMRequestV1 request = new FCMRequestV1(token, title, body, dataMap);

        // 3. Token lấy từ bước 1 (Google Playground)
        // Lưu ý: Phải có chữ "Bearer " đằng trước
        String accessToken = "Bearer " + ""; // nơi dán token

        // 4. Gửi
        RetrofitClient.getClient().sendNotification(accessToken, request)
                .enqueue(new Callback<FCMResponse>() {
                    @Override
                    public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                        if (response.isSuccessful()) {
                            Log.d("FCM", "Gửi tin V1 thành công!");
                        } else {
                            // In lỗi ra để xem
                            try {
                                Log.e("FCM", "Lỗi: " + response.errorBody().string());
                            } catch (Exception e) {}
                        }
                    }

                    @Override
                    public void onFailure(Call<FCMResponse> call, Throwable t) {
                        Log.e("FCM", "Lỗi mạng: " + t.getMessage());
                    }
                });
    }
}