package com.example.medibookandroid.ui.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.medibookandroid.ui.adapter.PatientAppointmentAdapter;
import com.example.medibookandroid.databinding.FragmentAppointmentsListBinding;
import com.example.medibookandroid.data.model.Appointment;
import com.example.medibookandroid.ui.patient.viewmodel.PatientAppointmentsViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AppointmentsListFragment extends Fragment implements PatientAppointmentAdapter.OnAppointmentCancelListener {

    private static final String ARG_STATUS_TYPE = "status_type";
    private FragmentAppointmentsListBinding binding;
    private PatientAppointmentsViewModel viewModel;
    private PatientAppointmentAdapter adapter;
    private List<Appointment> filteredAppointments;
    private String statusType; // "Upcoming", "History", hoặc "cancelled"

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

        // Lấy ViewModel được chia sẻ từ Fragment CHA
        try {
            viewModel = new ViewModelProvider(requireParentFragment()).get(PatientAppointmentsViewModel.class);
        } catch (IllegalStateException e) {
            // Fallback nếu có lỗi (an toàn)
            viewModel = new ViewModelProvider(requireActivity()).get(PatientAppointmentsViewModel.class);
        }
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

        filteredAppointments = new ArrayList<>();

        // ⭐️ BẮT ĐẦU SỬA LỖI 2 ⭐️
        // Khởi tạo Adapter với 4 tham số
        adapter = new PatientAppointmentAdapter(filteredAppointments, viewModel, this, getViewLifecycleOwner());
        // ⭐️ KẾT THÚC SỬA LỖI 2 ⭐️

        binding.rvAppointments.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvAppointments.setAdapter(adapter);

        // Lắng nghe dữ liệu
        setupObservers();
    }

    /**
     * Tách riêng logic lắng nghe
     */
    private void setupObservers() {
        if (viewModel == null) return;

        // 1. Lắng nghe trạng thái TẢI (Loading)
        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading == null) return;

            if (isLoading) {
                // Đang tải: Hiển thị ProgressBar
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.rvAppointments.setVisibility(View.GONE);
                binding.tvNoAppointments.setVisibility(View.GONE);
            } else {
                // Tải xong: Ẩn ProgressBar
                binding.progressBar.setVisibility(View.GONE);
                // Kiểm tra lại xem list (đã lọc) có rỗng không
                updateEmptyView();
            }
        });

        // 2. Lắng nghe danh sách TẤT CẢ lịch hẹn
        viewModel.getAllAppointments().observe(getViewLifecycleOwner(), allAppointments -> {
            if (allAppointments != null) {
                filterAndDisplay(allAppointments); // Lọc và hiển thị
            }
        });

        // 3. Lắng nghe thông báo (Toast) - (Chủ yếu cho lỗi hủy lịch)
        viewModel.getToastMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Logic chính: Lọc và hiển thị các lịch hẹn
     */
    private void filterAndDisplay(List<Appointment> allAppointments) {
        if (allAppointments == null || binding == null) return;

        // 1. Xác định các trạng thái thực tế cần lọc
        List<String> statusesToFilter;
        if (statusType.equals("Upcoming")) {
            statusesToFilter = Arrays.asList("pending", "confirmed");
        } else if (statusType.equals("History")) {
            statusesToFilter = Arrays.asList("completed");
        } else { // "cancelled"
            statusesToFilter = Arrays.asList("cancelled");
        }

        // 2. Lọc danh sách
        List<Appointment> appointments = allAppointments.stream()
                .filter(a -> a.getStatus() != null && statusesToFilter.contains(a.getStatus().toLowerCase()))
                .collect(Collectors.toList());

        // 3. Cập nhật Adapter
        filteredAppointments.clear();
        filteredAppointments.addAll(appointments);
        adapter.notifyDataSetChanged();

        // 4. Hiển thị/Ẩn thông báo "Không có lịch hẹn"
        // (Chỉ cập nhật nếu không còn đang tải)
        if(viewModel.isLoading().getValue() != null && !viewModel.isLoading().getValue()) {
            updateEmptyView();
        }
    }

    private void updateEmptyView() {
        if (filteredAppointments.isEmpty()) {
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