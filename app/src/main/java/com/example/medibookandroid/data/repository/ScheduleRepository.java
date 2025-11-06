package com.example.medibookandroid.data.repository; // Tạo package repository

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.medibookandroid.data.model.Appointment;
import com.example.medibookandroid.data.model.DoctorSchedule;
import com.example.medibookandroid.data.model.Patient;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class ScheduleRepository {
    private static final String TAG = "ScheduleRepository";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Lấy ca làm việc (DoctorSchedules)
    public void getSchedules(String doctorId, String date, MutableLiveData<List<DoctorSchedule>> schedulesLiveData) {
        db.collection("doctor_schedules")
                .whereEqualTo("doctorId", doctorId)
                .whereEqualTo("date", date)
                .whereEqualTo("available", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // ⭐️⭐️ THÊM DÒNG NÀY VÀO ⭐️⭐️
                    Log.d(TAG, "Query getSchedules thành công! Tìm thấy: " + queryDocumentSnapshots.size() + " ca làm việc.");
                    // ⭐️⭐️ KẾT THÚC ⭐️⭐️
                    schedulesLiveData.setValue(queryDocumentSnapshots.toObjects(DoctorSchedule.class));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting schedules: ", e);
                    schedulesLiveData.setValue(new ArrayList<>()); // Trả về list rỗng nếu lỗi
                });
    }

    // Thêm ca làm việc
    public void addSchedule(DoctorSchedule schedule, OnOperationCompleteListener listener) { // <-- SỬA Ở ĐÂY
        db.collection("doctor_schedules").add(schedule)
                .addOnSuccessListener(documentReference -> listener.onComplete(true)) // <-- SỬA Ở ĐÂY
                .addOnFailureListener(e -> listener.onComplete(false)); // <-- SỬA Ở ĐÂY
    }

    // Sửa ca làm việc
    public void updateSchedule(DoctorSchedule schedule, OnOperationCompleteListener listener) { // <-- SỬA Ở ĐÂY
        if (schedule.getScheduleId() == null) {
            listener.onComplete(false); // <-- SỬA Ở ĐÂY
            return;
        }
        db.collection("doctor_schedules").document(schedule.getScheduleId())
                .set(schedule)
                .addOnSuccessListener(aVoid -> listener.onComplete(true)) // <-- SỬA Ở ĐÂY
                .addOnFailureListener(e -> listener.onComplete(false)); // <-- SỬA Ở ĐÂY
    }

    // Xóa ca làm việc
    public void deleteSchedule(String scheduleId, OnOperationCompleteListener listener) { // <-- SỬA Ở ĐÂY
        db.collection("doctor_schedules").document(scheduleId)
                .delete()
                .addOnSuccessListener(aVoid -> listener.onComplete(true)) // <-- SỬA Ở ĐÂY
                .addOnFailureListener(e -> listener.onComplete(false)); // <-- SỬA Ở ĐÂY
    }

    // ⭐️⭐️ BẮT ĐẦU HÀM MỚI (DÙNG CHO PATIENTVIEWMODEL) ⭐️⭐️
    /**
     * Lấy TẤT CẢ các ca làm việc CÒN TRỐNG của một bác sĩ
     * (Dùng cho bệnh nhân xem lịch)
     */
    public LiveData<List<DoctorSchedule>> getSchedulesForDoctor(String doctorId) {
        MutableLiveData<List<DoctorSchedule>> schedulesLiveData = new MutableLiveData<>();

        db.collection("doctor_schedules")
                .whereEqualTo("doctorId", doctorId)
                .whereEqualTo("available", true) // ⭐️ Bệnh nhân chỉ thấy ca còn trống
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Query getSchedulesForDoctor thành công! Tìm thấy: " + queryDocumentSnapshots.size());
                    schedulesLiveData.setValue(queryDocumentSnapshots.toObjects(DoctorSchedule.class));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting schedules cho doctor " + doctorId, e);
                    schedulesLiveData.setValue(new ArrayList<>());
                });

        return schedulesLiveData; // Trả về LiveData
    }
    // ⭐️⭐️ KẾT THÚC HÀM MỚI ⭐️⭐️
}