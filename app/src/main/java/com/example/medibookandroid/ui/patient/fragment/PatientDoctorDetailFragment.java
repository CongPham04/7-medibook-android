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
import com.example.medibookandroid.R;
// ⭐️ THÊM IMPORT ADAPTER MỚI
import com.example.medibookandroid.ui.adapter.ReviewAdapter;
import com.example.medibookandroid.ui.adapter.TimeSlotAdapter;
import com.example.medibookandroid.databinding.FragmentPatientDoctorDetailBinding;
import com.example.medibookandroid.data.model.Doctor;
import com.example.medibookandroid.data.model.DoctorSchedule;
// ⭐️ THÊM IMPORT MODEL REVIEW (NẾU CHƯA CÓ)
import com.example.medibookandroid.data.model.Review;
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
    private ReviewAdapter reviewAdapter; // ⭐️ THÊM MỚI
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
        // Trong onViewCreated
        binding.calendarView.setMinDate(System.currentTimeMillis());

        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        viewModel = new ViewModelProvider(this).get(PatientViewModel.class);
        selectedDate = Calendar.getInstance();

        String doctorId = getArguments() != null ? getArguments().getString("doctorId") : null;

        if (doctorId == null) {
            Toast.makeText(getContext(), "Lỗi: Không tìm thấy ID bác sĩ", Toast.LENGTH_SHORT).show();
            navController.popBackStack();
            return;
        }

        setupRecyclerView();
        setupListeners();
        setupObservers(doctorId); // Bắt đầu quan sát

        viewModel.getSchedulesForDoctor(doctorId); // Tải TẤT CẢ ca làm việc

        // ⭐️ THÊM MỚI: Yêu cầu tải reviews
        viewModel.getReviewsForDoctor(doctorId);
    }

    private void setupRecyclerView() {
        // 1. TimeSlot Adapter (như cũ)
        timeSlotAdapter = new TimeSlotAdapter(new ArrayList<>(), schedule -> {
            selectedSchedule = schedule;
        });
        binding.rvTimeSlots.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvTimeSlots.setAdapter(timeSlotAdapter);

        // ⭐️ THÊM MỚI: Review Adapter
        // (LayoutManager đã được set trong XML, nhưng set lại ở đây cho chắc chắn)
        reviewAdapter = new ReviewAdapter(new ArrayList<>());
        binding.rvReviews.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvReviews.setAdapter(reviewAdapter);
    }

    private void setupObservers(String doctorId) {
        // 1. Quan sát chi tiết bác sĩ (như cũ)
        viewModel.getDoctorById(doctorId).observe(getViewLifecycleOwner(), doctor -> {
            if (doctor != null) {
                this.selectedDoctor = doctor;
                binding.tvDoctorName.setText("Bs. " + doctor.getFullName());
                binding.tvDoctorSpecialty.setText("Chuyên khoa " + doctor.getSpecialty());
                binding.tvDoctorQualificationValue.setText(doctor.getQualifications());
                binding.tvDoctorWorkplaceValue.setText(doctor.getWorkplace());
                // binding.rvReviews // ⭐️ XÓA DÒNG LỖI NÀY

                if (doctor.getAvatarUrl() != null && !doctor.getAvatarUrl().isEmpty() && getContext() != null) {
                    Glide.with(getContext())
                            .load(doctor.getAvatarUrl())
                            .placeholder(R.drawable.logo2)
                            .circleCrop()
                            .into(binding.ivDoctorAvatar);
                }
            }
        });

        // 2. Quan sát TẤT CẢ ca làm việc (như cũ)
        viewModel.getSchedulesForDoctor(doctorId).observe(getViewLifecycleOwner(), schedules -> {
            if (schedules != null) {
                allAvailableSchedules.clear();
                allAvailableSchedules.addAll(schedules);
                updateAvailableTimeSlots();
            }
        });

        // ⭐️ THÊM MỚI: Quan sát danh sách đánh giá
        viewModel.getReviewsForDoctor(doctorId).observe(getViewLifecycleOwner(), reviews -> {
            if (reviews != null && !reviews.isEmpty()) {
                // Có đánh giá
                reviewAdapter.updateData(reviews);
                binding.rvReviews.setVisibility(View.VISIBLE);
                binding.tvNoReviewsAvailable.setVisibility(View.GONE); // Ẩn text
            } else {
                // Không có đánh giá
                binding.rvReviews.setVisibility(View.GONE);
                binding.tvNoReviewsAvailable.setVisibility(View.VISIBLE); // Hiển thị text
            }
        });
    }

    // (Hàm setupListeners giữ nguyên)
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

    // (Hàm updateAvailableTimeSlots giữ nguyên)
    private void updateAvailableTimeSlots() {
        if (allAvailableSchedules.isEmpty()) {
            binding.tvNoSlotsAvailable.setVisibility(View.VISIBLE);
            binding.rvTimeSlots.setVisibility(View.GONE);
            return;
        }

        String dateString = firestoreDateFormat.format(selectedDate.getTime());
        // --- ⭐️ BỔ SUNG LOGIC THỜI GIAN (Hoàn) ⭐️ ---
        Calendar now = Calendar.getInstance();
        String todayString = firestoreDateFormat.format(now.getTime());

        // Biến này phải là "effectively final" (không gán lại sau lần này)
        final boolean isToday = dateString.equals(todayString);

        // Khai báo 2 biến final
        final int finalCurrentHour;
        final int finalCurrentMinute;

        if (isToday) {
            // Gán giá trị 1 lần duy nhất cho các biến final
            finalCurrentHour = now.get(Calendar.HOUR_OF_DAY); // 24h format
            finalCurrentMinute = now.get(Calendar.MINUTE);
        } else {
            // Gán giá trị mặc định nếu không phải hôm nay
            finalCurrentHour = -1;
            finalCurrentMinute = -1;
        }
        // --- ⭐️ KẾT THÚC SỬA ⭐️ ---
        List<DoctorSchedule> filteredSlots = allAvailableSchedules.stream()
                .filter(schedule -> {
                    if (schedule.getDate() == null || !schedule.getDate().equals(dateString)) {
                        return false; // Lọc theo ngày (như cũ)
                    }
                    // ⭐️ SỬA: Dùng biến final trong lambda ⭐️
                    if (isToday) {
                        try {
                            // Giả sử startTime là "09:00"
                            String[] timeParts = schedule.getStartTime().split(":");
                            int slotHour = Integer.parseInt(timeParts[0]);
                            int slotMinute = Integer.parseInt(timeParts[1]);

                            // So sánh với các biến final
                            if (slotHour < finalCurrentHour) return false;
                            if (slotHour == finalCurrentHour && slotMinute <= finalCurrentMinute) return false;

                        } catch (Exception e) {
                            Log.e("TimeSlotFilter", "Lỗi parse giờ: " + schedule.getStartTime());
                            return false; // Bỏ qua nếu giờ bị lỗi
                        }
                    }
                    return true; // Vượt qua tất cả
                })
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