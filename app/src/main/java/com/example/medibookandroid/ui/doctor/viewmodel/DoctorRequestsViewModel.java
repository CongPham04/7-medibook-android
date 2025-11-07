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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DoctorRequestsViewModel extends ViewModel {

    private final AppointmentRepository appointmentRepository;
    private final NotificationRepository notificationRepository;
    private final DoctorRepository doctorRepository;
    private final String currentDoctorId;

    private LiveData<Doctor> currentDoctor;

    // ⭐️ SỬA: Thêm isLoading
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<List<Appointment>> pendingRequests = new MutableLiveData<>();
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();

    private static final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public DoctorRequestsViewModel() {
        this.appointmentRepository = new AppointmentRepository();
        this.notificationRepository = new NotificationRepository();
        this.doctorRepository = new DoctorRepository();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            this.currentDoctorId = user.getUid();
            this.currentDoctor = doctorRepository.getDoctorById(currentDoctorId);
            // ⭐️ SỬA: Bắt đầu lắng nghe (listen) ngay lập tức
            listenForPendingRequests();
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
    // ⭐️ THÊM: Getter cho isLoading
    public LiveData<Boolean> isLoading() {
        return _isLoading;
    }

    /**
     * ⭐️ SỬA: Đổi tên hàm
     * Bắt đầu lắng nghe các yêu cầu đang chờ (pending)
     */
    private void listenForPendingRequests() {
        if (currentDoctorId.equals("ERROR_NO_USER")) {
            toastMessage.setValue("Lỗi xác thực bác sĩ");
            return;
        }
        // ⭐️ SỬA: Gọi hàm listener mới, truyền _isLoading vào
        appointmentRepository.listenForPendingAppointments(currentDoctorId, pendingRequests, _isLoading);
    }

    /**
     * Bác sĩ chấp nhận lịch hẹn
     */
    public void acceptAppointment(Appointment appointment) {
        // Chỉ cập nhật status (không động đến schedule)
        appointmentRepository.updateAppointmentStatus(appointment.getAppointmentId(), "confirmed", success -> {
            if (success) {
                toastMessage.setValue("Đã xác nhận lịch hẹn");
                // ⭐️ XÓA: loadPendingRequests() (Snapshot listener tự cập nhật)

                // Logic Tạo Thông Báo
                Doctor doctor = currentDoctor.getValue();
                String doctorName = (doctor != null) ? doctor.getFullName() : "Bác sĩ";

                String displayDate = appointment.getDate();
                try {
                    Date date = inputFormat.parse(appointment.getDate());
                    if (date != null) {
                        displayDate = outputFormat.format(date);
                    }
                } catch (Exception e) { /* Bỏ qua */ }

                String title = "✔ Lịch hẹn đã được xác nhận!";
                String message = "Lịch hẹn của bạn với Bác sĩ " + doctorName +
                        " vào lúc " + appointment.getTime() + ", " + displayDate + " đã được xác nhận.";
                Notification notif = new Notification(appointment.getPatientId(), title, message, "booking_confirmed");
                notificationRepository.createNotification(notif);

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
                // ⭐️ XÓA: loadPendingRequests() (Snapshot listener tự cập nhật)

                // Logic Tạo Thông Báo
                Doctor doctor = currentDoctor.getValue();
                String doctorName = (doctor != null) ? doctor.getFullName() : "Bác sĩ";

                String displayDate = appointment.getDate();
                try {
                    Date date = inputFormat.parse(appointment.getDate());
                    if (date != null) {
                        displayDate = outputFormat.format(date);
                    }
                } catch (Exception e) { /* Bỏ qua */ }

                String title = "❌ Lịch hẹn đã bị từ chối!";
                String message = "Lịch hẹn của bạn với " + doctorName +
                        " vào lúc " + appointment.getTime() + ", " + displayDate + " đã bị từ chối.";
                Notification notif = new Notification(appointment.getPatientId(), title, message, "booking_declined");
                notificationRepository.createNotification(notif);

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