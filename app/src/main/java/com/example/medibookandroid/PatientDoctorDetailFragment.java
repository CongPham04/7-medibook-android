package com.example.medibookandroid;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.medibookandroid.adapter.TimeSlotAdapter;
import com.example.medibookandroid.databinding.FragmentPatientDoctorDetailBinding;
// THÊM IMPORT
import com.example.medibookandroid.model.Appointment;
import com.example.medibookandroid.model.Doctor;
import com.example.medibookandroid.model.StorageRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;

// THÊM IMPORT
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

public class PatientDoctorDetailFragment extends Fragment {

    private FragmentPatientDoctorDetailBinding binding;
    private StorageRepository storageRepository;
    private Doctor selectedDoctor;
    private String selectedTimeSlot = null;

    // --- BẮT ĐẦU THAY ĐỔI ---
    private Calendar selectedDate;
    private TimeSlotAdapter timeSlotAdapter;
    private List<String> availableTimeSlots = new ArrayList<>();
    // --- KẾT THÚC THAY ĐỔI ---

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPatientDoctorDetailBinding.inflate(inflater, container, false);
        storageRepository = StorageRepository.getInstance(getContext());
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
        final NavController navController = Navigation.findNavController(view);

        // Khởi tạo ngày được chọn là hôm nay
        selectedDate = Calendar.getInstance();

        int doctorId = getArguments().getInt("doctorId");
        selectedDoctor = storageRepository.findDoctorById(doctorId);

        if (selectedDoctor != null) {
            binding.tvDoctorName.setText(selectedDoctor.getName());
            binding.tvDoctorSpecialty.setText(selectedDoctor.getSpecialty());
            // TODO: In a real app, you'd load more details like experience, workplace, etc.
        }

        // --- BẮT ĐẦU THAY ĐỔI: Thiết lập Adapter và Calendar ---

        // 1. Thiết lập Adapter (với danh sách rỗng ban đầu)
        timeSlotAdapter = new TimeSlotAdapter(availableTimeSlots, time -> {
            selectedTimeSlot = time;
        });
        binding.rvTimeSlots.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvTimeSlots.setAdapter(timeSlotAdapter);

        // 2. Thêm trình xử lý cho CalendarView
        binding.calendarView.setOnDateChangeListener((v, year, month, dayOfMonth) -> {
            selectedDate.set(year, month, dayOfMonth);
            updateAvailableTimeSlots();
        });

        // 3. Tải các ca làm việc cho ngày hôm nay (lần đầu)
        updateAvailableTimeSlots();

        // --- KẾT THÚC THAY ĐỔI ---


        binding.btnBookAppointment.setOnClickListener(v -> {
            if (selectedTimeSlot != null) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("doctor", selectedDoctor);
                bundle.putString("time", selectedTimeSlot);
                // THÊM MỚI: Truyền cả ngày đã chọn
                bundle.putSerializable("date", selectedDate.getTime());
                navController.navigate(R.id.action_patientDoctorDetailFragment_to_bookingSummaryFragment, bundle);
            } else {
                Toast.makeText(getContext(), "Vui lòng chọn một ca làm việc", Toast.LENGTH_SHORT).show();
            }
        });

        binding.toolbar.setNavigationOnClickListener(v -> navController.popBackStack());
    }

    // --- BẮT ĐẦU HÀM MỚI ---
    /**
     * Lọc và hiển thị các ca làm việc (status: "Available")
     * dựa trên bác sĩ và ngày đã chọn.
     */
    private void updateAvailableTimeSlots() {
        if (storageRepository == null || selectedDoctor == null) return;

        int doctorId = selectedDoctor.getId();

        List<String> slots = storageRepository.appointments.stream()
                // Lọc theo ID bác sĩ
                .filter(a -> a.getDoctorId() == doctorId)
                // Lọc theo trạng thái "Available"
                .filter(a -> a.getStatus().equalsIgnoreCase("Available"))
                // Lọc theo ngày đã chọn
                .filter(a -> {
                    if (a.getAppointmentDate() == null) return false;
                    Calendar apptCal = Calendar.getInstance();
                    apptCal.setTime(a.getAppointmentDate());
                    return apptCal.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR) &&
                            apptCal.get(Calendar.MONTH) == selectedDate.get(Calendar.MONTH) &&
                            apptCal.get(Calendar.DAY_OF_MONTH) == selectedDate.get(Calendar.DAY_OF_MONTH);
                })
                // Trích xuất chuỗi thời gian (ví dụ: "09:00 - 10:00")
                .map(Appointment::getSymptoms)
                .collect(Collectors.toList());

        // Cập nhật Adapter
        availableTimeSlots.clear();
        availableTimeSlots.addAll(slots);
        timeSlotAdapter.notifyDataSetChanged();

        // Đặt lại lựa chọn
        selectedTimeSlot = null;
        timeSlotAdapter.resetSelection(); // Bạn sẽ cần thêm hàm này vào TimeSlotAdapter

        // Ẩn/hiện text "Không có ca làm việc"
        if (slots.isEmpty()) {
            binding.tvNoSlotsAvailable.setVisibility(View.VISIBLE);
            binding.rvTimeSlots.setVisibility(View.GONE);
        } else {
            binding.tvNoSlotsAvailable.setVisibility(View.GONE);
            binding.rvTimeSlots.setVisibility(View.VISIBLE);
        }
    }
    // --- KẾT THÚC HÀM MỚI ---

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
