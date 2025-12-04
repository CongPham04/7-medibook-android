package com.example.medibookandroid.ui.patient.fragment;

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

import com.example.medibookandroid.R;
import com.example.medibookandroid.data.model.DoctorSchedule;
import com.example.medibookandroid.databinding.FragmentBookingSummaryBinding;
import com.example.medibookandroid.data.model.Appointment;
import com.example.medibookandroid.data.model.Doctor;
import com.example.medibookandroid.ui.patient.viewmodel.PatientViewModel;
// ⭐️ IMPORT HELPER ĐỂ PHÁT LOA ⭐️
import com.example.medibookandroid.data.repository.NotificationHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BookingSummaryFragment extends Fragment {

    private FragmentBookingSummaryBinding binding;
    private PatientViewModel viewModel;
    private NavController navController;

    private Doctor selectedDoctor;
    private DoctorSchedule selectedSchedule;
    private String currentPatientId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBookingSummaryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

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

        // 1. Lấy thông tin người dùng
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Lỗi xác thực, vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            navController.popBackStack(R.id.patientHomeFragment, false);
            return;
        }
        currentPatientId = currentUser.getUid();

        // 2. Lấy dữ liệu từ arguments
        try {
            selectedDoctor = (Doctor) getArguments().getSerializable("doctor");
            selectedSchedule = (DoctorSchedule) getArguments().getSerializable("schedule");

            if (selectedDoctor == null || selectedSchedule == null) {
                throw new NullPointerException("Dữ liệu lịch hẹn không hợp lệ");
            }
        } catch (Exception e) {
            Log.e("BookingSummary", "Không thể lấy arguments", e);
            Toast.makeText(getContext(), "Lỗi khi tải dữ liệu lịch hẹn", Toast.LENGTH_SHORT).show();
            navController.popBackStack();
            return;
        }

        populateSummaryCard();
        setupListeners();
        setupObservers();
    }

    private void populateSummaryCard() {
        binding.tvDoctorNameSummary.setText("Bs. " + selectedDoctor.getFullName());

        String displayDate = selectedSchedule.getDate();

        if (selectedSchedule.getDate() != null && !selectedSchedule.getDate().isEmpty()) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                Date date = inputFormat.parse(selectedSchedule.getDate());
                if (date != null) {
                    displayDate = outputFormat.format(date);
                }
            } catch (Exception e) {
                Log.e("BookingSummary", "Lỗi định dạng ngày: " + e.getMessage());
            }
        }

        String dateTime = "Thời gian: " + displayDate +
                ", lúc " + selectedSchedule.getStartTime() +
                " - " + selectedSchedule.getEndTime();
        binding.tvAppointmentTimeSummary.setText(dateTime);
    }

    private void setupListeners() {
        binding.btnConfirmBooking.setOnClickListener(v -> {
            String symptoms = binding.tilSymptoms.getEditText().getText().toString().trim();
            if(symptoms.isEmpty()){
                binding.tilSymptoms.setError("Vui lòng mô tả tình trạng");
                return;
            }
            binding.tilSymptoms.setError(null);

            Appointment newAppointment = new Appointment(
                    currentPatientId,
                    selectedDoctor.getDoctorId(),
                    selectedSchedule.getScheduleId(),
                    selectedSchedule.getDate(),
                    selectedSchedule.getStartTime(),
                    symptoms,
                    "pending"
            );

            // Gọi ViewModel để tạo lịch (ViewModel sẽ xử lý việc bắn Notification)
            viewModel.createAppointment(newAppointment, selectedDoctor);

            binding.btnConfirmBooking.setEnabled(false);
            binding.btnConfirmBooking.setText("Đang xử lý...");
        });

        binding.toolbar.setNavigationOnClickListener(v -> navController.popBackStack());
    }

    // ⭐️ PHẦN QUAN TRỌNG NHẤT ĐÃ SỬA ⭐️
    private void setupObservers() {
        // 1. Quan sát trạng thái tạo lịch hẹn (để điều hướng UI)
        viewModel.getAppointmentCreationStatus().observe(getViewLifecycleOwner(), success -> {
            if (success == null) return;

            if (Boolean.TRUE.equals(success)) {
                // Điều hướng sang màn hình thành công
                navController.navigate(R.id.action_bookingSummaryFragment_to_bookingSuccessFragment);
            } else {
                Toast.makeText(getContext(), "Đặt lịch thất bại, vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                binding.btnConfirmBooking.setEnabled(true);
                binding.btnConfirmBooking.setText("Xác nhận Đặt lịch");
            }
        });

        // 2. ⭐️ Quan sát Thông báo thành công (để PHÁT LOA & RUNG) ⭐️
        viewModel.getBookingSuccessNotification().observe(getViewLifecycleOwner(), notification -> {
            if (notification != null) {
                // Gọi Helper để hiển thị thông báo hệ thống (có âm thanh tùy chỉnh)
                NotificationHelper.showBookingNotification(
                        requireContext(),
                        notification.getTitle(),
                        notification.getMessage()
                );
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}