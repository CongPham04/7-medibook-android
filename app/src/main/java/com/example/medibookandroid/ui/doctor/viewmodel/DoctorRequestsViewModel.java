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

import java.util.List;

public class DoctorRequestsViewModel extends ViewModel {

    private final AppointmentRepository appointmentRepository;
    private final NotificationRepository notificationRepository;
    private final DoctorRepository doctorRepository;
    private final String currentDoctorId;

    private LiveData<Doctor> currentDoctor; // Để lấy tên Bác sĩ

    private final MutableLiveData<List<Appointment>> pendingRequests = new MutableLiveData<>();
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();
    // LiveData để tải thông tin bệnh nhân (cho Adapter)
    // private final MutableLiveData<Patient> patientInfo = new MutableLiveData<>(); // (Dòng này không cần thiết vì Adapter tự quản lý)


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
                String title = "Lịch hẹn đã được xác nhận";
                String message = "Lịch hẹn của bạn với " + doctorName +
                        " vào lúc " + appointment.getTime() + ", " + appointment.getDate() + " đã được xác nhận.";
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
                loadPendingRequests(); // Tải lại danh sách

                // Logic Tạo Thông Báo
                Doctor doctor = currentDoctor.getValue();
                String doctorName = (doctor != null) ? doctor.getFullName() : "Bác sĩ";
                String title = "Lịch hẹn đã bị từ chối";
                String message = "Lịch hẹn của bạn với " + doctorName +
                        " vào lúc " + appointment.getTime() + ", " + appointment.getDate() + " đã bị từ chối.";
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