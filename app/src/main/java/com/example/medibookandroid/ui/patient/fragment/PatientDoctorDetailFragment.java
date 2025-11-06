package com.example.medibookandroid.ui.patient.fragment; // ⭐️ SỬA PACKAGE NẾU CẦN

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.medibookandroid.R; // ⭐️ THÊM IMPORT
import com.example.medibookandroid.ui.adapter.TimeSlotAdapter;
import com.example.medibookandroid.databinding.FragmentPatientDoctorDetailBinding;
import com.example.medibookandroid.data.model.Doctor;
import com.example.medibookandroid.data.model.DoctorSchedule;
import com.example.medibookandroid.ui.patient.viewmodel.PatientViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class PatientDoctorDetailFragment extends Fragment {

    private FragmentPatientDoctorDetailBinding binding;
    private PatientViewModel viewModel;
    private NavController navController;

    private TimeSlotAdapter timeSlotAdapter;
    private Doctor selectedDoctor = null;
    private DoctorSchedule selectedSchedule = null;
    private Calendar selectedDate;
    private List<DoctorSchedule> allAvailableSchedules = new ArrayList<>();
    private final SimpleDateFormat firestoreDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPatientDoctorDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    // (Các hàm onResume, onPause giữ nguyên)
    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.patient_bottom_nav);
            if (bottomNav != null) {
                bottomNav.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() != null) {
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.patient_bottom_nav);
            if (bottomNav != null) {
                bottomNav.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        viewModel = new ViewModelProvider(this).get(PatientViewModel.class);
        selectedDate = Calendar.getInstance();

        // ⭐️ SỬA LỖI Ở ĐÂY ⭐️
        // Lấy doctorId (phải là String)
        String doctorId = getArguments() != null ? getArguments().getString("doctorId") : null;

        if (doctorId == null) {
            // ⭐️ KẾT THÚC SỬA LỖI ⭐️
            Toast.makeText(getContext(), "Lỗi: Không tìm thấy ID bác sĩ", Toast.LENGTH_SHORT).show();
            navController.popBackStack();
            return;
        }

        setupRecyclerView();
        setupListeners();
        setupObservers(doctorId); // Bắt đầu quan sát

        // Yêu cầu ViewModel tải data
        // ⭐️ SỬA LỖI Ở ĐÂY ⭐️ (gọi hàm đã có trong ViewModel)
        viewModel.getSchedulesForDoctor(doctorId); // Tải TẤT CẢ ca làm việc
    }

    private void setupRecyclerView() {
        timeSlotAdapter = new TimeSlotAdapter(new ArrayList<>(), schedule -> {
            selectedSchedule = schedule;
        });
        binding.rvTimeSlots.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvTimeSlots.setAdapter(timeSlotAdapter);
    }

    private void setupObservers(String doctorId) {
        // 1. Quan sát chi tiết bác sĩ
        viewModel.getDoctorById(doctorId).observe(getViewLifecycleOwner(), doctor -> {
            if (doctor != null) {
                this.selectedDoctor = doctor;
                binding.tvDoctorName.setText(doctor.getFullName());
                binding.tvDoctorSpecialty.setText(doctor.getSpecialty());
                binding.tvDoctorQualificationValue.setText(doctor.getQualifications());
                binding.tvDoctorWorkplaceValue.setText(doctor.getWorkplace());

                if (doctor.getAvatarUrl() != null && !doctor.getAvatarUrl().isEmpty() && getContext() != null) {
                    Glide.with(getContext())
                            .load(doctor.getAvatarUrl())
                            .placeholder(R.drawable.logo2)
                            .circleCrop()
                            .into(binding.ivDoctorAvatar);
                }
            }
        });

        // 2. Quan sát TẤT CẢ ca làm việc của bác sĩ này
        // ⭐️ SỬA LỖI Ở ĐÂY ⭐️ (gọi hàm đã có trong ViewModel)
        viewModel.getSchedulesForDoctor(doctorId).observe(getViewLifecycleOwner(), schedules -> {
            if (schedules != null) {
                allAvailableSchedules.clear();
                allAvailableSchedules.addAll(schedules);
                updateAvailableTimeSlots();
            }
        });
    }

    private void setupListeners() {
        binding.calendarView.setOnDateChangeListener((v, year, month, dayOfMonth) -> {
            selectedDate.set(year, month, dayOfMonth);
            updateAvailableTimeSlots();
        });

        binding.btnBookAppointment.setOnClickListener(v -> {
            if (selectedSchedule != null && selectedDoctor != null) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("doctor", (Serializable) selectedDoctor);
                bundle.putSerializable("schedule", (Serializable) selectedSchedule);

                // (Giả sử BookingSummaryFragment tồn tại)
                navController.navigate(R.id.action_patientDoctorDetailFragment_to_bookingSummaryFragment, bundle);
                Toast.makeText(getContext(), "Chuyển sang màn hình xác nhận...", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(getContext(), "Vui lòng chọn bác sĩ và ca làm việc", Toast.LENGTH_SHORT).show();
            }
        });

        binding.toolbar.setNavigationOnClickListener(v -> navController.popBackStack());
    }

    private void updateAvailableTimeSlots() {
        if (allAvailableSchedules.isEmpty()) {
            binding.tvNoSlotsAvailable.setVisibility(View.VISIBLE);
            binding.rvTimeSlots.setVisibility(View.GONE);
            return;
        }

        String dateString = firestoreDateFormat.format(selectedDate.getTime());

        List<DoctorSchedule> filteredSlots = allAvailableSchedules.stream()
                .filter(schedule -> schedule.getDate() != null && schedule.getDate().equals(dateString))
                .collect(Collectors.toList());

        timeSlotAdapter.updateData(filteredSlots);
        selectedSchedule = null;
        timeSlotAdapter.resetSelection();

        if (filteredSlots.isEmpty()) {
            binding.tvNoSlotsAvailable.setVisibility(View.VISIBLE);
            binding.rvTimeSlots.setVisibility(View.GONE);
        } else {
            binding.tvNoSlotsAvailable.setVisibility(View.GONE);
            binding.rvTimeSlots.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
