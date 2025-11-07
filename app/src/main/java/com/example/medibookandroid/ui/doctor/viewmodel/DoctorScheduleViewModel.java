package com.example.medibookandroid.ui.doctor.viewmodel;

import android.text.TextUtils;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.medibookandroid.data.model.Appointment;
import com.example.medibookandroid.data.model.DoctorSchedule;
import com.example.medibookandroid.data.model.Patient;

// ⭐️ SỬA: Import cả 2 repository
import com.example.medibookandroid.data.repository.AppointmentRepository;
import com.example.medibookandroid.data.repository.ScheduleRepository;
import com.example.medibookandroid.data.repository.OnOperationCompleteListener; // ⭐️ THÊM

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DoctorScheduleViewModel extends ViewModel {

    // ⭐️ SỬA: Tách thành 2 repository
    private final ScheduleRepository scheduleRepository;
    private final AppointmentRepository appointmentRepository;

    private final String currentDoctorId; // UID của bác sĩ đang đăng nhập
    private final SimpleDateFormat firestoreDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    // ⭐️ BẮT ĐẦU SỬA: Thêm LiveData cho loading ⭐️
    private final MutableLiveData<Boolean> _isLoadingAvailable = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> _isLoadingConfirmed = new MutableLiveData<>(false);
    // ⭐️ KẾT THÚC SỬA ⭐️

    // LiveData cho View "quan sát"
    private final MutableLiveData<List<DoctorSchedule>> availableSlots = new MutableLiveData<>();
    private final MutableLiveData<List<Appointment>> confirmedAppointments = new MutableLiveData<>();
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> completionStatus = new MutableLiveData<>();


    public DoctorScheduleViewModel() {
        // ⭐️ SỬA: Khởi tạo cả 2 repository
        this.scheduleRepository = new ScheduleRepository();
        this.appointmentRepository = new AppointmentRepository();

        // Lấy UID của bác sĩ hiện tại
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            this.currentDoctorId = user.getUid();
        } else {
            // Đây là giải pháp tạm thời nếu chưa có Auth
            this.currentDoctorId = "uid_xyz789"; // !!! THAY BẰNG UID THẬT
            Log.w("ViewModel", "FirebaseUser is null, using hardcoded Doctor UID.");
        }
    }

    // Getters cho LiveData
    public LiveData<List<DoctorSchedule>> getAvailableSlots() {
        return availableSlots;
    }
    public LiveData<List<Appointment>> getConfirmedAppointments() {
        return confirmedAppointments;
    }
    public LiveData<String> getToastMessage() {
        toastMessage.setValue(null); // Reset
        return toastMessage;
    }
    public LiveData<Boolean> getCompletionStatus() {
        completionStatus.setValue(null); // Reset
        return completionStatus;
    }

    // ⭐️ THÊM: Getters cho loading ⭐️
    public LiveData<Boolean> isLoadingAvailable() {
        return _isLoadingAvailable;
    }
    public LiveData<Boolean> isLoadingConfirmed() {
        return _isLoadingConfirmed;
    }

    // Hàm View gọi khi đổi ngày
    public void loadDataForDate(Date date) {
        String dateString = firestoreDateFormat.format(date);
        Log.d("ScheduleVM", "Đang tải data cho Doctor ID: " + currentDoctorId + " | Ngày: " + dateString);

        // ⭐️ SỬA: Gọi hàm repo 4 tham số
        scheduleRepository.getSchedules(currentDoctorId, dateString, availableSlots, _isLoadingAvailable);
        appointmentRepository.getConfirmedAppointmentsForDoctorByDate(currentDoctorId, dateString, confirmedAppointments, _isLoadingConfirmed);
    }

    // Hàm View gọi khi lưu ca làm việc mới
    public void createScheduleSlot(String dateString, String startTime, String endTime) {
        if (!isTimeValid(startTime, endTime)) return;

        DoctorSchedule newSchedule = new DoctorSchedule(currentDoctorId, dateString, startTime, endTime, true);

        // (Giữ nguyên) Gọi scheduleRepository
        scheduleRepository.addSchedule(newSchedule, success -> {
            if (success) {
                toastMessage.setValue("Đã lưu ca làm việc mới");
                refreshData(dateString); // Tải lại data cho ngày đó
            } else {
                toastMessage.setValue("Lỗi khi lưu ca làm việc");
            }
        });
    }

    // Hàm View gọi khi sửa ca làm việc
    public void updateScheduleSlot(DoctorSchedule schedule, String startTime, String endTime) {
        if (!isTimeValid(startTime, endTime)) return;

        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);

        // (Giữ nguyên) Gọi scheduleRepository
        scheduleRepository.updateSchedule(schedule, success -> {
            if (success) {
                toastMessage.setValue("Đã cập nhật ca làm việc");
                refreshData(schedule.getDate()); // Tải lại data
            } else {
                toastMessage.setValue("Lỗi khi cập nhật");
            }
        });
    }

    // Hàm View gọi khi xóa ca làm việc
    public void deleteScheduleSlot(DoctorSchedule schedule) {
        // (Giữ nguyên) Gọi scheduleRepository
        scheduleRepository.deleteSchedule(schedule.getScheduleId(), success -> {
            if (success) {
                toastMessage.setValue("Đã xóa ca làm việc");
                refreshData(schedule.getDate()); // Tải lại data
            } else {
                toastMessage.setValue("Lỗi khi xóa");
            }
        });
    }

    /**
     * Bác sĩ đánh dấu lịch hẹn là "Đã hoàn thành"
     */
    public void markAsCompleted(Appointment appointment) {
        if (!"confirmed".equalsIgnoreCase(appointment.getStatus())) {
            toastMessage.setValue("Lịch hẹn chưa được xác nhận");
            return;
        }

        appointmentRepository.updateAppointmentStatus(appointment.getAppointmentId(), "completed", success -> {
            if (success) {
                toastMessage.setValue("Đã hoàn tất lịch hẹn");
                // Tải lại dữ liệu cho ngày hiện tại
                refreshData(appointment.getDate());
                completionStatus.postValue(true);
            } else {
                toastMessage.setValue("Lỗi: Không thể hoàn tất lịch hẹn");
                completionStatus.postValue(false);
            }
        });
    }

    // Hàm tải thông tin bệnh nhân cho Adapter
    public void loadPatientInfo(String patientId, MutableLiveData<Patient> patientLiveData) {
        // ⭐️ SỬA: Gọi appointmentRepository
        appointmentRepository.getPatientInfo(patientId, patientLiveData);
    }


    // Tải lại data sau khi CUD (Giữ nguyên)
    private void refreshData(String dateString) {
        try {
            Date date = firestoreDateFormat.parse(dateString);
            if (date != null) loadDataForDate(date);
        } catch (ParseException e) {
            Log.e("ViewModel", "Date parse error on refresh", e);
        }
    }

    // Logic kiểm tra thời gian (Giữ nguyên)
    private boolean isTimeValid(String startTime, String endTime) {
        if (TextUtils.isEmpty(startTime) || TextUtils.isEmpty(endTime)) {
            toastMessage.setValue("Vui lòng chọn giờ bắt đầu và kết thúc");
            return false;
        }
        try {
            Date start = timeFormat.parse(startTime);
            Date end = timeFormat.parse(endTime);
            if (start.after(end) || start.equals(end)) {
                toastMessage.setValue("Giờ kết thúc phải sau giờ bắt đầu");
                return false;
            }
        } catch (ParseException e) {
            toastMessage.setValue("Lỗi định dạng thời gian");
            return false;
        }
        return true;
    }
}