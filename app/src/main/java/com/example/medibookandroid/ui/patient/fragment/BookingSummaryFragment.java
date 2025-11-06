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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.Serializable;
import java.util.Date;

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
        // Lấy PatientViewModel (được chia sẻ từ Activity hoặc tạo mới)
        viewModel = new ViewModelProvider(this).get(PatientViewModel.class);

        // 1. Lấy thông tin người dùng (Patient)
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Lỗi xác thực, vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            navController.popBackStack(R.id.patientHomeFragment, false); // Quay về Home
            return;
        }
        currentPatientId = currentUser.getUid();

        // 2. Lấy dữ liệu (Doctor và Schedule) từ arguments
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

        // 3. Hiển thị thông tin lên UI
        populateSummaryCard();

        // 4. Setup Listeners
        setupListeners();

        // 5. Setup Observers (Lắng nghe kết quả)
        setupObservers();
    }

    private void populateSummaryCard() {
        binding.tvDoctorNameSummary.setText(selectedDoctor.getFullName());

        String dateTime = selectedSchedule.getDate() +
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
                    selectedSchedule.getStartTime(), // Dùng giờ bắt đầu làm "time"
                    symptoms,
                    "pending" // Trạng thái ban đầu
            );

            // Gọi viewModel.createAppointment (truyền cả doctor để tạo thông báo)
            viewModel.createAppointment(newAppointment, selectedDoctor);

            // Vô hiệu hóa nút để tránh click 2 lần
            binding.btnConfirmBooking.setEnabled(false);
            binding.btnConfirmBooking.setText("Đang xử lý...");
        });

        binding.toolbar.setNavigationOnClickListener(v -> navController.popBackStack());
    }

    private void setupObservers() {
        viewModel.getAppointmentCreationStatus().observe(getViewLifecycleOwner(), success -> {
            // Chỉ chạy khi success không phải null
            if (success == null) {
                return;
            }

            if (Boolean.TRUE.equals(success)) {
                // Thành công, điều hướng
                navController.navigate(R.id.action_bookingSummaryFragment_to_bookingSuccessFragment);
            } else {
                // Thất bại
                Toast.makeText(getContext(), "Đặt lịch thất bại, vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                // Kích hoạt lại nút
                binding.btnConfirmBooking.setEnabled(true);
                binding.btnConfirmBooking.setText("Xác nhận Đặt lịch");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}