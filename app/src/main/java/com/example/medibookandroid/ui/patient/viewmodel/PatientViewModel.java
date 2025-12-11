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
import com.example.medibookandroid.data.model.Review;
import com.example.medibookandroid.data.repository.AppointmentRepository;
import com.example.medibookandroid.data.repository.DoctorRepository;
import com.example.medibookandroid.data.repository.PatientRepository;
import com.example.medibookandroid.data.repository.ReviewRepository;
import com.example.medibookandroid.data.repository.ScheduleRepository;
import com.example.medibookandroid.data.repository.NotificationRepository;
import com.example.medibookandroid.utils.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PatientViewModel extends ViewModel {

    private PatientRepository patientRepository;
    private DoctorRepository doctorRepository;
    private AppointmentRepository appointmentRepository;
    private ScheduleRepository doctorScheduleRepository;
    private NotificationRepository notificationRepository;
    private ReviewRepository reviewRepository;

    private static final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    // LiveData cho Patient
    private MutableLiveData<String> patientId = new MutableLiveData<>();
    private LiveData<Patient> currentPatient;

    // --- PHẦN TÌM KIẾM ---
    // 1. List gốc chứa toàn bộ bác sĩ tải từ DB
    private List<Doctor> originalDoctorList = new ArrayList<>();
    // 2. LiveData chứa danh sách bác sĩ để hiển thị (đã qua lọc)
    private MutableLiveData<List<Doctor>> displayedDoctors = new MutableLiveData<>();

    // ⭐️ BỔ SUNG BIẾN NÀY (Đã sửa lỗi thiếu biến)
    private String currentSearchQuery = "";

    private MutableLiveData<Notification> _bookingSuccessNotification = new MutableLiveData<>();
    private MutableLiveData<Boolean> appointmentCreationStatus = new MutableLiveData<>();
    private MutableLiveData<Boolean> updatePatientStatus = new MutableLiveData<>();

    public PatientViewModel() {
        patientRepository = new PatientRepository();
        doctorRepository = new DoctorRepository();
        appointmentRepository = new AppointmentRepository();
        doctorScheduleRepository = new ScheduleRepository();
        notificationRepository = new NotificationRepository();
        reviewRepository = new ReviewRepository();

        // Load thông tin Patient
        currentPatient = Transformations.switchMap(patientId, id ->
                patientRepository.getPatientById(id)
        );

        // Bắt đầu lắng nghe dữ liệu bác sĩ Realtime
        startListeningToDoctors();
    }

    // ⭐️ LOGIC MỚI: Lắng nghe dữ liệu realtime
    private void startListeningToDoctors() {
        // observeForever để đảm bảo ViewModel luôn nhận được update dù Fragment có đang active hay không
        doctorRepository.getAllDoctorsRealtime().observeForever(doctors -> {
            if (doctors != null) {
                // 1. Cập nhật danh sách gốc
                originalDoctorList.clear();
                originalDoctorList.addAll(doctors);

                // 2. Lọc lại dữ liệu ngay lập tức dựa trên từ khóa đang nhập
                // (Để cập nhật số sao mới lên giao diện ngay cả khi đang tìm kiếm)
                filterDoctors(currentSearchQuery);
            }
        });
    }

    // ⭐️ LOGIC MỚI: Tách hàm lọc ra để tái sử dụng
    public void setSearchQuery(String query) {
        this.currentSearchQuery = query; // Lưu lại từ khóa vào biến toàn cục
        filterDoctors(query);
    }

    private void filterDoctors(String query) {
        if (originalDoctorList == null || originalDoctorList.isEmpty()) {
            return;
        }

        // Nếu query rỗng hoặc null, hiển thị toàn bộ danh sách gốc
        if (query == null || query.trim().isEmpty()) {
            displayedDoctors.setValue(new ArrayList<>(originalDoctorList)); // Trả về bản sao để an toàn
            return;
        }

        List<Doctor> filteredList = new ArrayList<>();
        String normalizedQuery = StringUtils.removeAccent(query.trim());

        for (Doctor doctor : originalDoctorList) {
            String name = StringUtils.removeAccent(doctor.getFullName());
            String specialty = StringUtils.removeAccent(doctor.getSpecialty());

            if (name.contains(normalizedQuery) || specialty.contains(normalizedQuery)) {
                filteredList.add(doctor);
            }
        }
        displayedDoctors.setValue(filteredList);
    }

    // Getter trả về LiveData displayedDoctors
    public LiveData<List<Doctor>> getDoctors() {
        return displayedDoctors;
    }

    // --- CÁC HÀM KHÁC GIỮ NGUYÊN ---

    public LiveData<Patient> getPatient() {
        return currentPatient;
    }

    public void loadPatient(String id) {
        patientId.setValue(id);
    }

    public void updatePatient(Patient patient) {
        patientRepository.updatePatient(patient, success -> {
            updatePatientStatus.postValue(success);
        });
    }

    public LiveData<Boolean> getUpdatePatientStatus() {
        updatePatientStatus.setValue(null);
        return updatePatientStatus;
    }

    public LiveData<Doctor> getDoctorById(String doctorId) {
        return doctorRepository.getDoctorById(doctorId);
    }

    public LiveData<List<Appointment>> getAppointmentsForPatient(String patientId) {
        MutableLiveData<Boolean> dummyLoading = new MutableLiveData<>();
        return appointmentRepository.getAppointmentsForPatient(patientId, dummyLoading);
    }

    public void createAppointment(Appointment appointment, Doctor doctor) {
        appointmentRepository.createAppointment(appointment, success -> {
            appointmentCreationStatus.postValue(success);

            if (Boolean.TRUE.equals(success)) {
                String displayDate = appointment.getDate();
                try {
                    Date date = inputFormat.parse(appointment.getDate());
                    if (date != null) {
                        displayDate = outputFormat.format(date);
                    }
                } catch (Exception e) { /* Ignore */ }

                String title = "✔ Đặt lịch thành công!";
                String message = "Bạn đã đặt lịch khám với Bác sĩ " + doctor.getFullName() +
                        " vào lúc " + appointment.getTime() + ", " + displayDate + ".";

                Notification notif = new Notification(appointment.getPatientId(), title, message, "booking_success");
                notificationRepository.createNotification(notif);
                _bookingSuccessNotification.postValue(notif);
            }
        });
    }

    public LiveData<Boolean> getAppointmentCreationStatus() {
        appointmentCreationStatus.setValue(null);
        return appointmentCreationStatus;
    }
    public LiveData<Notification> getBookingSuccessNotification() {
        _bookingSuccessNotification.setValue(null);
        return _bookingSuccessNotification;
    }
    public LiveData<List<DoctorSchedule>> getSchedulesForDoctor(String doctorId) {
        return doctorScheduleRepository.getSchedulesForDoctor(doctorId);
    }

    public LiveData<List<Review>> getReviewsForDoctor(String doctorId) {
        return reviewRepository.getReviewsForDoctor(doctorId);
    }
}