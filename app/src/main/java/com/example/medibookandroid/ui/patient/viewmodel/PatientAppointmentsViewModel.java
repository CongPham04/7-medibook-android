package com.example.medibookandroid.ui.patient.viewmodel;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import com.example.medibookandroid.data.model.Appointment;
import com.example.medibookandroid.data.model.Doctor;
import com.example.medibookandroid.data.model.Notification;
import com.example.medibookandroid.data.model.Patient;
import com.example.medibookandroid.data.model.Review;
import com.example.medibookandroid.data.repository.AppointmentRepository;
import com.example.medibookandroid.data.repository.DoctorRepository;
import com.example.medibookandroid.data.repository.NotificationRepository;

import com.example.medibookandroid.data.repository.PatientRepository;
import com.example.medibookandroid.data.repository.ReviewRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// ⭐️ THÊM CÁC IMPORT NÀY ⭐️
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
// ⭐️ KẾT THÚC THÊM ⭐️

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ViewModel riêng biệt, được CHIA SẺ cho PatientAppointmentsFragment
 * và các Fragment con (AppointmentsListFragment) trong ViewPager.
 */
public class PatientAppointmentsViewModel extends ViewModel {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository; // 1. ⭐️ Khai báo thêm Repo này
    private final NotificationRepository notificationRepository;
    private final ReviewRepository reviewRepository;
    private final String currentPatientId;

    // ⭐️ BẮT ĐẦU SỬA: Thêm 2 định dạng ngày ⭐️
    // Định dạng (Format) của ngày lưu trên Firestore ("2025-11-06")
    private static final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    // Định dạng bạn muốn hiển thị ("06/11/2025")
    private static final SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    // ⭐️ KẾT THÚC SỬA ⭐️

    // LiveData chứa TẤT CẢ lịch hẹn của bệnh nhân
    private MutableLiveData<List<Appointment>> allAppointments = new MutableLiveData<>();

    // LiveData cho trạng thái tải
    private MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);

    // LiveData cho thông báo (Toast)
    private MutableLiveData<String> toastMessage = new MutableLiveData<>();
    // LiveData cho trạng thái hủy
    private MutableLiveData<Boolean> cancellationStatus = new MutableLiveData<>();

    // Cache để giữ thông tin bác sĩ, tránh gọi Firestore lặp lại
    private Map<String, LiveData<Doctor>> doctorCache = new HashMap<>();

    public PatientAppointmentsViewModel() {
        this.appointmentRepository = new AppointmentRepository();
        this.doctorRepository = new DoctorRepository();
        this.patientRepository = new PatientRepository(); // 2. ⭐️ Khởi tạo Repo này
        this.notificationRepository = new NotificationRepository();
        this.reviewRepository = new ReviewRepository();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            this.currentPatientId = user.getUid();
        } else {
            this.currentPatientId = "ERROR_NO_USER";
            Log.e("PatientApptsVM", "FirebaseUser is null!");
        }
    }

    // --- Getters ---
    public LiveData<List<Appointment>> getAllAppointments() {
        return allAppointments;
    }
    public LiveData<String> getToastMessage() {
        toastMessage.setValue(null); // Reset
        return toastMessage;
    }
    public LiveData<Boolean> isLoading() {
        return _isLoading;
    }
    public LiveData<Boolean> getCancellationStatus() {
        cancellationStatus.setValue(null); // Reset
        return cancellationStatus;
    }

    /**
     * Gửi đánh giá (ĐÃ SỬA ĐỂ LẤY TÊN BỆNH NHÂN)
     */
    public void submitReview(String appointmentId, String doctorId, float rating, String comment) {
        if (currentPatientId.equals("ERROR_NO_USER")) return;

        _isLoading.setValue(true);

        // 3. ⭐️ Gọi Repository lấy thông tin bệnh nhân trước
        patientRepository.getPatientById(currentPatientId).observeForever(new Observer<Patient>() {
            @Override
            public void onChanged(Patient patient) {
                // Quan trọng: Gỡ bỏ observer ngay để tránh bị gọi lại nhiều lần
                patientRepository.getPatientById(currentPatientId).removeObserver(this);

                String realPatientName = "Ẩn danh";
                // Lấy tên thật nếu có
                if (patient != null && patient.getFullName() != null && !patient.getFullName().isEmpty()) {
                    realPatientName = patient.getFullName();
                }

                // Tạo review với tên thật vừa lấy được
                Review review = new Review(doctorId, currentPatientId, realPatientName, rating, comment);

                // Gửi Review lên Firestore
                reviewRepository.createReview(review, appointmentId, success -> {
                    _isLoading.setValue(false);
                    if (success) {
                        toastMessage.setValue("Cảm ơn đánh giá của bạn!");
                        loadAppointments();
                    } else {
                        toastMessage.setValue("Lỗi khi gửi đánh giá.");
                    }
                });
            }
        });
    }

    /**
     * Tải (hoặc tải lại) tất cả lịch hẹn của bệnh nhân.
     * Được gọi bởi Fragment cha (PatientAppointmentsFragment).
     */
    /**
     * Tải (hoặc tải lại) tất cả lịch hẹn của bệnh nhân.
     */
    public void loadAppointments() {
        if (currentPatientId.equals("ERROR_NO_USER")) {
            toastMessage.setValue("Lỗi xác thực người dùng");
            return;
        }

        // Đặt loading = true để giao diện biết đang tải lại
        _isLoading.setValue(true);

        // Lưu ý: getAppointmentsForPatient cần trả về LiveData MỚI hoặc cập nhật data
        appointmentRepository.getAppointmentsForPatient(currentPatientId, _isLoading)
                .observeForever(appointments -> {
                    // Cập nhật vào LiveData quản lý danh sách
                    allAppointments.setValue(appointments);

                    // Tắt loading (Dù repo đã tắt, nhưng set lại cho chắc chắn UI cập nhật)
                    _isLoading.setValue(false);
                });
    }

    /**
     * Bệnh nhân hủy một lịch hẹn
     */
    public void cancelAppointment(Appointment appointment) {
        // Biến cờ để đảm bảo observeForever chỉ chạy 1 lần
        AtomicBoolean notificationSent = new AtomicBoolean(false);

        // Gọi hàm để hủy lịch VÀ mở lại ca làm việc
        appointmentRepository.cancelAppointmentAndFreeSlot(appointment, success -> {
            if (success) {
                toastMessage.setValue("Đã hủy lịch hẹn");
                cancellationStatus.postValue(true);
                loadAppointments(); // Tải lại toàn bộ danh sách

                // Logic Tạo Thông Báo
                // Tải thông tin bác sĩ để chèn vào thông báo
                getDoctorById(appointment.getDoctorId()).observeForever(doctor -> {
                    // Chỉ tạo thông báo 1 lần
                    if (doctor != null && !notificationSent.get()) {
                        notificationSent.set(true); // Đánh dấu đã gửi

                        // ⭐️ BẮT ĐẦU SỬA: Logic định dạng ngày cho thông báo ⭐️
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

                        String title = "❌ Đã hủy lịch hẹn!";
                        String message = "Bạn đã hủy lịch hẹn với Bác sĩ " + doctor.getFullName() +
                                " vào lúc " + appointment.getTime() + ", " + displayDate + "."; // Dùng displayDate
                        Notification notif = new Notification(currentPatientId, title, message, "booking_cancelled");
                        notificationRepository.createNotification(notif);
                        // ⭐️ KẾT THÚC SỬA ⭐️
                    }
                });

            } else {
                toastMessage.setValue("Lỗi khi hủy lịch hẹn");
                cancellationStatus.postValue(false);
            }
        });
    }

    /**
     * Lấy thông tin bác sĩ theo ID (có cache)
     * Được gọi bởi Adapter (ViewHolder).
     */
    public LiveData<Doctor> getDoctorById(String doctorId) {
        if (doctorCache.containsKey(doctorId)) {
            return doctorCache.get(doctorId);
        } else {
            LiveData<Doctor> doctorData = doctorRepository.getDoctorById(doctorId);
            doctorCache.put(doctorId, doctorData);
            return doctorData;
        }
    }
}