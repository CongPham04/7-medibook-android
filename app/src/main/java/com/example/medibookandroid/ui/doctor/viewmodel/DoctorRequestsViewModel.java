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
            listenForPendingRequests();
        } else {
            this.currentDoctorId = "ERROR_NO_USER";
            Log.e("DoctorRequestsViewModel", "FirebaseUser is null!");
        }
    }

    // --- Getters cho LiveData ---
    public LiveData<List<Appointment>> getPendingRequests() { return pendingRequests; }
    public LiveData<String> getToastMessage() {
        toastMessage.setValue(null);
        return toastMessage;
    }
    public LiveData<Boolean> isLoading() { return _isLoading; }

    private void listenForPendingRequests() {
        if (currentDoctorId.equals("ERROR_NO_USER")) {
            toastMessage.setValue("Lỗi xác thực bác sĩ");
            return;
        }
        appointmentRepository.listenForPendingAppointments(currentDoctorId, pendingRequests, _isLoading);
    }

    /**
     * Bác sĩ chấp nhận lịch hẹn
     */
    public void acceptAppointment(Appointment appointment) {
        // ⭐️ SỬA QUAN TRỌNG: Thêm tham số patientId
        appointmentRepository.updateAppointmentStatus(
                appointment.getAppointmentId(),
                "confirmed",
                appointment.getPatientId(), // <-- Thêm cái này
                success -> {
                    if (success) {
                        toastMessage.setValue("Đã xác nhận lịch hẹn");

                        // --- Logic Tạo Thông Báo (Lưu vào DB cho lịch sử) ---
                        // (Việc gửi FCM Rung/Chuông đã được Repository xử lý rồi)
                        Doctor doctor = currentDoctor.getValue();
                        String doctorName = (doctor != null) ? doctor.getFullName() : "Bác sĩ";

                        String displayDate = appointment.getDate();
                        try {
                            Date date = inputFormat.parse(appointment.getDate());
                            if (date != null) displayDate = outputFormat.format(date);
                        } catch (Exception e) {}

                        String title = "✔ Lịch hẹn đã được xác nhận!";
                        String message = "Lịch hẹn của bạn với Bác sĩ " + doctorName +
                                " vào lúc " + appointment.getTime() + ", " + displayDate + " đã được xác nhận.";
                        Notification notif = new Notification(appointment.getPatientId(), title, message, "booking_confirmed");
                        notificationRepository.createNotification(notif);

                    } else {
                        toastMessage.setValue("Lỗi khi xác nhận lịch hẹn");
                    }
                }
        );
    }

    /**
     * Bác sĩ từ chối lịch hẹn
     */
    public void declineAppointment(Appointment appointment) {
        // Lưu ý: Hàm cancelAppointmentAndFreeSlot hiện tại chưa hỗ trợ gửi FCM tự động.
        // Bạn có thể giữ nguyên hoặc sửa Repository nếu muốn.

        appointmentRepository.cancelAppointmentAndFreeSlot(appointment, success -> {
            if (success) {
                toastMessage.setValue("Đã từ chối lịch hẹn");

                // Logic Tạo Thông Báo (Lưu vào DB)
                Doctor doctor = currentDoctor.getValue();
                String doctorName = (doctor != null) ? doctor.getFullName() : "Bác sĩ";

                String displayDate = appointment.getDate();
                try {
                    Date date = inputFormat.parse(appointment.getDate());
                    if (date != null) displayDate = outputFormat.format(date);
                } catch (Exception e) {}

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

    public void loadPatientInfo(String patientId, MutableLiveData<Patient> patientLiveData) {
        appointmentRepository.getPatientInfo(patientId, patientLiveData);
    }
}