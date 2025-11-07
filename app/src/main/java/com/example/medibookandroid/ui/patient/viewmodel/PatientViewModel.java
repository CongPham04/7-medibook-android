package com.example.medibookandroid.ui.patient.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.medibookandroid.data.model.Appointment;
import com.example.medibookandroid.data.model.Doctor;
import com.example.medibookandroid.data.model.DoctorSchedule;
import com.example.medibookandroid.data.model.Patient;
import com.example.medibookandroid.data.model.Notification;
import com.example.medibookandroid.data.model.Review; // (Giả sử bạn có model này)
import com.example.medibookandroid.data.repository.AppointmentRepository;
import com.example.medibookandroid.data.repository.DoctorRepository;
import com.example.medibookandroid.data.repository.PatientRepository;
import com.example.medibookandroid.data.repository.ReviewRepository; // (Giả sử bạn có repo này)
import com.example.medibookandroid.data.repository.ScheduleRepository;
import com.example.medibookandroid.data.repository.NotificationRepository;
import com.example.medibookandroid.data.repository.OnOperationCompleteListener;

import java.text.SimpleDateFormat; // ⭐️ THÊM IMPORT
import java.util.Date; // ⭐️ THÊM IMPORT
import java.util.List;
import java.util.Locale; // ⭐️ THÊM IMPORT

public class PatientViewModel extends ViewModel {

    private PatientRepository patientRepository;
    private DoctorRepository doctorRepository;
    private AppointmentRepository appointmentRepository;
    private ScheduleRepository doctorScheduleRepository;
    private NotificationRepository notificationRepository;
    private ReviewRepository reviewRepository;

    // ⭐️ BẮT ĐẦU SỬA: Thêm 2 định dạng ngày ⭐️
    // Định dạng (Format) của ngày lưu trên Firestore ("2025-11-06")
    private static final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    // Định dạng bạn muốn hiển thị ("06/11/2025")
    private static final SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    // ⭐️ KẾT THÚC SỬA ⭐️

    // LiveData cho Patient (Lấy theo ID)
    private MutableLiveData<String> patientId = new MutableLiveData<>();
    private LiveData<Patient> currentPatient;

    // LiveData cho Doctor (Tìm kiếm)
    private MutableLiveData<String> searchQuery = new MutableLiveData<>();
    private LiveData<List<Doctor>> doctorList;

    // LiveData cho trạng thái tạo Appointment
    private MutableLiveData<Boolean> appointmentCreationStatus = new MutableLiveData<>();

    // LiveData cho trạng thái CẬP NHẬT Patient
    private MutableLiveData<Boolean> updatePatientStatus = new MutableLiveData<>();

    public PatientViewModel() {
        patientRepository = new PatientRepository();
        doctorRepository = new DoctorRepository();
        appointmentRepository = new AppointmentRepository();
        doctorScheduleRepository = new ScheduleRepository();
        notificationRepository = new NotificationRepository();
        reviewRepository = new ReviewRepository(); // (Giả sử bạn có repo này)

        // Khi patientId thay đổi, gọi getPatientById
        currentPatient = Transformations.switchMap(patientId, id ->
                patientRepository.getPatientById(id)
        );

        // Khi searchQuery thay đổi, gọi repo tương ứng
        doctorList = Transformations.switchMap(searchQuery, query -> {
            if (query == null || query.isEmpty()) {
                return doctorRepository.getAllDoctors();
            } else {
                return doctorRepository.searchDoctors(query);
            }
        });
    }

    // --- Các hàm cho Patient ---
    public LiveData<Patient> getPatient() {
        return currentPatient;
    }

    public void loadPatient(String id) {
        patientId.setValue(id);
    }

    /**
     * Cập nhật hồ sơ bệnh nhân.
     * Fragment sẽ gọi hàm này (1 tham số).
     */
    public void updatePatient(Patient patient) {
        // ViewModel sẽ xử lý callback (listener)
        patientRepository.updatePatient(patient, success -> {
            updatePatientStatus.postValue(success); // Cập nhật LiveData
        });
    }

    /**
     * Lấy LiveData trạng thái cập nhật (để Fragment observe)
     */
    public LiveData<Boolean> getUpdatePatientStatus() {
        updatePatientStatus.setValue(null); // Reset
        return updatePatientStatus;
    }

    // --- Các hàm cho Doctor ---
    public LiveData<List<Doctor>> getDoctors() {
        return doctorList;
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public LiveData<Doctor> getDoctorById(String doctorId) {
        return doctorRepository.getDoctorById(doctorId);
    }

    // --- Các hàm cho Appointment & Schedule ---

    /**
     * Lấy lịch hẹn cho Bệnh nhân (đã sửa để khớp với Repository)
     */
    public LiveData<List<Appointment>> getAppointmentsForPatient(String patientId) {
        // Tạo một LiveData "giả" (dummy) vì hàm repo yêu cầu nó
        MutableLiveData<Boolean> dummyLoading = new MutableLiveData<>();
        return appointmentRepository.getAppointmentsForPatient(patientId, dummyLoading);
    }

    /**
     * Gọi Repository để tạo lịch hẹn
     */
    public void createAppointment(Appointment appointment, Doctor doctor) {
        // Hàm này gọi repository và repository sẽ cập nhật
        // `appointmentCreationStatus` LiveData thông qua listener
        appointmentRepository.createAppointment(appointment, success -> {
            appointmentCreationStatus.postValue(success);

            // ⭐️ BẮT ĐẦU SỬA: Logic định dạng ngày cho thông báo ⭐️
            if (Boolean.TRUE.equals(success)) {

                // Định dạng lại ngày để hiển thị
                String displayDate = appointment.getDate(); // Mặc định là chuỗi gốc
                try {
                    Date date = inputFormat.parse(appointment.getDate());
                    if (date != null) {
                        displayDate = outputFormat.format(date); // Chuyển sang "dd/MM/yyyy"
                    }
                } catch (Exception e) {
                    // Bỏ qua nếu lỗi, dùng ngày gốc
                }

                String title = "Đặt lịch thành công";
                String message = "Bạn đã đặt lịch khám thành công với Bác sĩ " + doctor.getFullName() +
                        " vào lúc " + appointment.getTime() + ", " + displayDate + "."; // Dùng displayDate
                Notification notif = new Notification(appointment.getPatientId(), title, message, "booking_success");
                notificationRepository.createNotification(notif);
            }
            // ⭐️ KẾT THÚC SỬA ⭐️
        });
    }

    /**
     * Lấy LiveData trạng thái tạo (để Fragment observe)
     */
    public LiveData<Boolean> getAppointmentCreationStatus() {
        appointmentCreationStatus.setValue(null); // Reset
        return appointmentCreationStatus;
    }

    public LiveData<List<DoctorSchedule>> getSchedulesForDoctor(String doctorId) {
        return doctorScheduleRepository.getSchedulesForDoctor(doctorId);
    }

    public LiveData<List<Review>> getReviewsForDoctor(String doctorId) {
        return reviewRepository.getReviewsForDoctor(doctorId);
    }
}