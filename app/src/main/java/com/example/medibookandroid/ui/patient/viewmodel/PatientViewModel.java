package com.example.medibookandroid.ui.patient.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import com.example.medibookandroid.data.model.Appointment;
import com.example.medibookandroid.data.model.Doctor;
import com.example.medibookandroid.data.model.DoctorSchedule;
import com.example.medibookandroid.data.model.Patient;
import com.example.medibookandroid.data.model.Notification; // ⭐️ THÊM
import com.example.medibookandroid.data.repository.AppointmentRepository;
import com.example.medibookandroid.data.repository.DoctorRepository;
import com.example.medibookandroid.data.repository.PatientRepository;
import com.example.medibookandroid.data.repository.ScheduleRepository;
import com.example.medibookandroid.data.repository.NotificationRepository; // ⭐️ THÊM
import com.example.medibookandroid.data.repository.OnOperationCompleteListener;

import java.util.List;

public class PatientViewModel extends ViewModel {

    private PatientRepository patientRepository;
    private DoctorRepository doctorRepository;
    private AppointmentRepository appointmentRepository;
    private ScheduleRepository doctorScheduleRepository;
    private NotificationRepository notificationRepository; // ⭐️ THÊM

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
        notificationRepository = new NotificationRepository(); // ⭐️ THÊM

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

    public void updatePatient(Patient patient) {
        patientRepository.updatePatient(patient, success -> {
            updatePatientStatus.postValue(success); // Cập nhật LiveData
        });
    }

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
    public LiveData<List<Appointment>> getAppointmentsForPatient(String patientId) {
        return appointmentRepository.getAppointmentsForPatient(patientId);
    }

    /**
     * Gọi Repository để tạo lịch hẹn
     */
    public void createAppointment(Appointment appointment, Doctor doctor) {
        // Hàm này gọi repository và repository sẽ cập nhật
        // `appointmentCreationStatus` LiveData thông qua listener
        appointmentRepository.createAppointment(appointment, success -> {
            appointmentCreationStatus.postValue(success);

            // Tạo thông báo nếu thành công
            if (Boolean.TRUE.equals(success)) {
                String title = "Đặt lịch thành công";
                String message = "Bạn đã đặt lịch khám thành công với " + doctor.getFullName() +
                        " vào lúc " + appointment.getTime() + ", " + appointment.getDate() + ".";
                Notification notif = new Notification(appointment.getPatientId(), title, message, "booking_success");
                notificationRepository.createNotification(notif);
            }
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
}

