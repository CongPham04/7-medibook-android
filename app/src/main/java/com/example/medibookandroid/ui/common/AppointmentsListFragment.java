package com.example.medibookandroid.ui.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider; // ⭐️ THÊM
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.medibookandroid.ui.adapter.PatientAppointmentAdapter; // ⭐️ SỬA
import com.example.medibookandroid.databinding.FragmentAppointmentsListBinding;
import com.example.medibookandroid.data.model.Appointment; // ⭐️ SỬA
import com.example.medibookandroid.ui.patient.viewmodel.PatientAppointmentsViewModel; // ⭐️ THÊM

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Fragment này đại diện cho MỘT tab (ví dụ: "Sắp tới")
 * và hiển thị danh sách các lịch hẹn tương ứng.
 */
public class AppointmentsListFragment extends Fragment implements PatientAppointmentAdapter.OnAppointmentCancelListener {

    private static final String ARG_STATUS_TYPE = "status_type";
    private FragmentAppointmentsListBinding binding;
    private PatientAppointmentsViewModel viewModel; // ⭐️ SỬA: Dùng ViewModel
    private PatientAppointmentAdapter adapter;
    private String statusType; // "Upcoming", "History", hoặc "Canceled"

    public static AppointmentsListFragment newInstance(String statusType) {
        AppointmentsListFragment fragment = new AppointmentsListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STATUS_TYPE, statusType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            statusType = getArguments().getString(ARG_STATUS_TYPE);
        }
        // ⭐️ SỬA: Lấy ViewModel được chia sẻ từ Fragment CHA
        viewModel = new ViewModelProvider(requireParentFragment()).get(PatientAppointmentsViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAppointmentsListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Khởi tạo Adapter với ViewModel
        // Chúng ta truyền this (LifecycleOwner) vào Adapter
        adapter = new PatientAppointmentAdapter(new ArrayList<>(), viewModel, this, this);

        binding.rvAppointments.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvAppointments.setAdapter(adapter);

        // 2. Lắng nghe dữ liệu
        setupObservers();
    }

    // ⭐️ XÓA: onResume() (Không cần tải lại data, LiveData tự làm)

    /**
     * Logic chính: Lắng nghe ViewModel, Lọc, và Hiển thị
     */
    private void setupObservers() {
        if (viewModel == null) return;

        viewModel.getAllAppointments().observe(getViewLifecycleOwner(), allAppointments -> {
            if (allAppointments == null) return;

            // 1. Xác định các trạng thái cần lọc
            List<String> statusesToFilter;
            if (statusType.equals("Upcoming")) {
                statusesToFilter = Arrays.asList("pending", "confirmed");
            } else if (statusType.equals("History")) {
                statusesToFilter = Arrays.asList("completed"); // ⭐️ SỬA: Giả sử có trạng thái này
            } else { // "Canceled"
                statusesToFilter = Arrays.asList("cancelled");
            }

            // 2. Lọc danh sách
            List<Appointment> filteredAppointments = allAppointments.stream()
                    .filter(a -> a.getStatus() != null && statusesToFilter.contains(a.getStatus()))
                    .collect(Collectors.toList());

            // 3. Cập nhật Adapter
            adapter.updateData(filteredAppointments);

            // 4. Hiển thị/Ẩn thông báo "Không có lịch hẹn"
            updateEmptyView(filteredAppointments.isEmpty());
        });
    }

    private void updateEmptyView(boolean isEmpty) {
        if (isEmpty) {
            binding.tvNoAppointments.setVisibility(View.VISIBLE);
            binding.rvAppointments.setVisibility(View.GONE);
        } else {
            binding.tvNoAppointments.setVisibility(View.GONE);
            binding.rvAppointments.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Được gọi khi bệnh nhân nhấn nút "Hủy" trên thẻ (từ Adapter)
     */
    @Override
    public void onCancelClick(Appointment appointment) {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận hủy lịch")
                .setMessage("Bạn có chắc chắn muốn hủy lịch hẹn này không?")
                .setPositiveButton("Hủy lịch", (dialog, which) -> {
                    // ⭐️ SỬA: Chỉ cần gọi ViewModel
                    viewModel.cancelAppointment(appointment);
                    dialog.dismiss();
                })
                .setNegativeButton("Không", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
