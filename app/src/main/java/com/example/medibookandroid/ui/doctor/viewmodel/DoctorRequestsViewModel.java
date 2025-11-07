package com.example.medibookandroid.ui.doctor.viewmodel;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.medibookandroid.data.model.Appointment;
import com.example.medibookandroid.data.model.Patient;
import com.example.medibookandroid.data.model.Doctor;
import com.example.medibookandroid.data.model.Notification;
import com.example.medibookandroid.data.repository.AppointmentRepository;
import com.example.medibookandroid.data.repository.DoctorRepository;
import com.example.medibookandroid.data.repository.NotificationRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// ⭐️ THÊM CÁC IMPORT NÀY ⭐️
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
// ⭐️ KẾT THÚC THÊM ⭐️

public class DoctorRequestsViewModel extends ViewModel {

    private final AppointmentRepository appointmentRepository;
    private final NotificationRepository notificationRepository;
    private final DoctorRepository doctorRepository;
    private final String currentDoctorId;

    private LiveData<Doctor> currentDoctor; // Để lấy tên Bác sĩ

    private final MutableLiveData<List<Appointment>> pendingRequests = new MutableLiveData<>();
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();

    // ⭐️ BẮT ĐẦU SỬA: Thêm 2 định dạng ngày ⭐️
    // Định dạng (Format) của ngày lưu trên Firestore ("2025-11-06")
    private static final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    // Định dạng bạn muốn hiển thị ("06/11/2025")
    private static final SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    // ⭐️ KẾT THÚC SỬA ⭐️

    public DoctorRequestsViewModel() {
        this.appointmentRepository = new AppointmentRepository();
        this.notificationRepository = new NotificationRepository();
        this.doctorRepository = new DoctorRepository();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            this.currentDoctorId = user.getUid();
            // Tải thông tin bác sĩ hiện tại để dùng cho thông báo
            this.currentDoctor = doctorRepository.getDoctorById(currentDoctorId);
        } else {
            this.currentDoctorId = "ERROR_NO_USER";
            Log.e("DoctorRequestsViewModel", "FirebaseUser is null!");
        }
    }

    // --- Getters cho LiveData ---
    public LiveData<List<Appointment>> getPendingRequests() {
        return pendingRequests;
    }
    public LiveData<String> getToastMessage() {
        toastMessage.setValue(null); // Reset
        return toastMessage;
    }

    /**
     * Tải các yêu cầu đang chờ (pending)
     */
    public void loadPendingRequests() {
        if (currentDoctorId.equals("ERROR_NO_USER")) {
            toastMessage.setValue("Lỗi xác thực bác sĩ");
            return;
        }
        appointmentRepository.getPendingAppointments(currentDoctorId, pendingRequests);
    }

    /**
     * Bác sĩ chấp nhận lịch hẹn
     */
    public void acceptAppointment(Appointment appointment) {
        // Chỉ cập nhật status (không động đến schedule)
        appointmentRepository.updateAppointmentStatus(appointment.getAppointmentId(), "confirmed", success -> {
            if (success) {
                toastMessage.setValue("Đã xác nhận lịch hẹn");
                loadPendingRequests(); // Tải lại danh sách

                // Logic Tạo Thông Báo
                Doctor doctor = currentDoctor.getValue();
                String doctorName = (doctor != null) ? doctor.getFullName() : "Bác sĩ";

                // ⭐️ BẮT ĐẦU SỬA: Logic định dạng ngày cho thông báo ⭐️
                String displayDate = appointment.getDate(); // Mặc định là chuỗi gốc
                try {
                    Date date = inputFormat.parse(appointment.getDate());
                    if (date != null) {
                        displayDate = outputFormat.format(date); // Chuyển sang "dd/MM/yyyy"
                    }
                } catch (Exception e) {
                    // Bỏ qua nếu lỗi, dùng ngày gốc
                }

                String title = "Lịch hẹn đã được xác nhận";
                String message = "Lịch hẹn của bạn với Bác sĩ " + doctorName +
                        " vào lúc " + appointment.getTime() + ", " + displayDate + " đã được xác nhận.";
                Notification notif = new Notification(appointment.getPatientId(), title, message, "booking_confirmed");
                notificationRepository.createNotification(notif);
                // ⭐️ KẾT THÚC SỬA ⭐️

            } else {
                toastMessage.setValue("Lỗi khi xác nhận lịch hẹn");
            }
        });
    }

    /**
     * Bác sĩ từ chối lịch hẹn
     */
    public void declineAppointment(Appointment appointment) {
        // Gọi hàm để hủy lịch VÀ mở lại ca
        appointmentRepository.cancelAppointmentAndFreeSlot(appointment, success -> {
            if (success) {
                toastMessage.setValue("Đã từ chối lịch hẹn");
                loadPendingRequests(); // Tải lại danh sách

                // Logic Tạo Thông Báo
                Doctor doctor = currentDoctor.getValue();
                String doctorName = (doctor != null) ? doctor.getFullName() : "Bác sĩ";

                // ⭐️ BẮT ĐẦU SỬA: Logic định dạng ngày cho thông báo ⭐️
                String displayDate = appointment.getDate(); // Mặc định là chuỗi gốc
                try {
                    Date date = inputFormat.parse(appointment.getDate());
                    if (date != null) {
                        displayDate = outputFormat.format(date); // Chuyển sang "dd/MM/yyyy"
                    }
                } catch (Exception e) {
                    // Bỏ qua nếu lỗi, dùng ngày gốc
                }

                String title = "Lịch hẹn đã bị từ chối";
                String message = "Lịch hẹn của bạn với Bác sĩ " + doctorName +
                        " vào lúc " + appointment.getTime() + ", " + displayDate + " đã bị từ chối.";
                Notification notif = new Notification(appointment.getPatientId(), title, message, "booking_declined");
                notificationRepository.createNotification(notif);
                // ⭐️ KẾT THÚC SỬA ⭐️

            } else {
                toastMessage.setValue("Lỗi khi từ chối lịch hẹn");
            }
        });
    }

    /**
     * Tải thông tin bệnh nhân (cho Adapter)
     */
    public void loadPatientInfo(String patientId, MutableLiveData<Patient> patientLiveData) {
        appointmentRepository.getPatientInfo(patientId, patientLiveData);
    }
}