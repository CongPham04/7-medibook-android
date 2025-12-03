package com.example.medibookandroid.ui.doctor.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.medibookandroid.data.model.Appointment;
import com.example.medibookandroid.data.model.Doctor;
import com.example.medibookandroid.data.model.DoctorSchedule;
import com.example.medibookandroid.data.repository.AppointmentRepository;
import com.example.medibookandroid.data.repository.DoctorRepository;
import com.example.medibookandroid.data.repository.ScheduleRepository;

import java.util.List;

public class DoctorViewModel extends ViewModel {

    private DoctorRepository doctorRepository;
    private AppointmentRepository appointmentRepository;
    private ScheduleRepository doctorScheduleRepository;

    // LiveData để giữ hồ sơ bác sĩ hiện tại
    private MutableLiveData<String> doctorId = new MutableLiveData<>();
    private LiveData<Doctor> currentDoctor;

    // LiveData cho trạng thái cập nhật
    private MutableLiveData<Boolean> updateDoctorStatus = new MutableLiveData<>();

    public DoctorViewModel() {
        doctorRepository = new DoctorRepository();
        appointmentRepository = new AppointmentRepository();
        doctorScheduleRepository = new ScheduleRepository(); // Giả sử tên file là ScheduleRepository

        // Khi doctorId thay đổi, gọi getDoctorById
        currentDoctor = Transformations.switchMap(doctorId, id ->
                doctorRepository.getDoctorById(id)
        );
    }

    // --- Các hàm cho Hồ sơ Bác sĩ (Profile) ---

    public LiveData<Doctor> getDoctor() {
        return currentDoctor;
    }

    public void loadDoctor(String id) {
        doctorId.setValue(id);
    }

    // Hàm gọi cập nhật
    public void updateDoctor(Doctor doctor) {
        doctorRepository.updateDoctor(doctor, success -> {
            updateDoctorStatus.postValue(success);
        });
    }

    // LiveData để quan sát kết quả cập nhật
    public LiveData<Boolean> getUpdateDoctorStatus() {
        updateDoctorStatus.setValue(null); // Reset
        return updateDoctorStatus;
    }

    // --- Các hàm cho các Fragment khác (giữ lại từ file của bạn) ---

    public LiveData<List<Appointment>> getAppointmentsForDoctor(String doctorId) {
        return appointmentRepository.getAppointmentsForDoctor(doctorId);
    }

    public LiveData<List<DoctorSchedule>> getSchedulesForDoctor(String doctorId) {
        return doctorScheduleRepository.getSchedulesForDoctor(doctorId);
    }

    // (Các hàm createSchedule, deleteSchedule... sẽ cần sửa Repository
    // giống như updateDoctor, nhưng chúng ta sẽ làm khi cần)

    public void confirmAppointment(Appointment appointment) {
        // ⭐️ SỬA: Dùng appointmentRepository
        appointmentRepository.updateAppointmentStatus(
                appointment.getAppointmentId(),
                "confirmed",
                appointment.getPatientId(),
                success -> {
                    if (success) {
                        // Toast: Đã xác nhận
                        // Reload list (nếu cần)
                    }
                }
        );
    }

    public void cancelAppointment(Appointment appointment) {
        // ⭐️ SỬA: Dùng appointmentRepository
        appointmentRepository.updateAppointmentStatus(
                appointment.getAppointmentId(),
                "cancelled",
                appointment.getPatientId(),
                success -> {
                    if (success) {
                        // Toast: Đã hủy
                    }
                }
        );
    }
}
