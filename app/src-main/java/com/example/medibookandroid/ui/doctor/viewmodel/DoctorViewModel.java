package com.example.medibookandroid.ui.doctor.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.medibookandroid.data.model.Appointment;
import com.example.medibookandroid.data.model.Doctor;
import com.example.medibookandroid.data.model.DoctorSchedule;
import com.example.medibookandroid.data.repository.AppointmentRepository;
import com.example.medibookandroid.data.repository.DoctorRepository;
import com.example.medibookandroid.data.repository.DoctorScheduleRepository;

import java.util.List;

public class DoctorViewModel extends ViewModel {

    private DoctorRepository doctorRepository;
    private AppointmentRepository appointmentRepository;
    private DoctorScheduleRepository doctorScheduleRepository;

    public DoctorViewModel() {
        doctorRepository = new DoctorRepository();
        appointmentRepository = new AppointmentRepository();
        doctorScheduleRepository = new DoctorScheduleRepository();
    }

    public LiveData<Doctor> getDoctorById(String doctorId) {
        return doctorRepository.getDoctorById(doctorId);
    }

    public LiveData<List<Appointment>> getAppointmentsForDoctor(String doctorId) {
        return appointmentRepository.getAppointmentsForDoctor(doctorId);
    }

    public LiveData<List<DoctorSchedule>> getSchedulesForDoctor(String doctorId) {
        return doctorScheduleRepository.getSchedulesForDoctor(doctorId);
    }

    public LiveData<Boolean> createSchedule(DoctorSchedule schedule) {
        return doctorScheduleRepository.createSchedule(schedule);
    }

    public LiveData<Boolean> updateDoctor(Doctor doctor) {
        return doctorRepository.updateDoctor(doctor);
    }

    public LiveData<Boolean> deleteSchedule(String scheduleId) {
        return doctorScheduleRepository.deleteSchedule(scheduleId);
    }
}
