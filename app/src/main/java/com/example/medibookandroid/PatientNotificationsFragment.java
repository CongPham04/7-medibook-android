package com.example.medibookandroid;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.medibookandroid.adapter.NotificationAdapter;
import com.example.medibookandroid.databinding.FragmentPatientNotificationsBinding;
// THÊM IMPORT
import com.example.medibookandroid.model.Notification;
import com.example.medibookandroid.model.StorageRepository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PatientNotificationsFragment extends Fragment {

    private FragmentPatientNotificationsBinding binding;
    // THÊM MỚI
    private StorageRepository storageRepository;
    private NotificationAdapter adapter;
    private List<Notification> patientNotifications;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPatientNotificationsBinding.inflate(inflater, container, false);
        // THÊM MỚI
        storageRepository = StorageRepository.getInstance(getContext());
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Tải lại dữ liệu khi quay lại màn hình
        loadNotifications();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Tải dữ liệu lần đầu
        loadNotifications();

        // Xử lý nút "Xóa tất cả"
        binding.tvToolbarDeleteAll.setOnClickListener(v -> {
            showDeleteAllConfirmation();
        });
    }

    private void loadNotifications() {
        // ID Bệnh nhân (Giả sử là 1 cho demo)
        int currentPatientId = 1; // TODO: Lấy ID bệnh nhân đang đăng nhập

        // SỬA: Lấy dữ liệu từ Repository
        patientNotifications = storageRepository.notifications.stream()
                .filter(n -> n.getPatientId() == currentPatientId)
                .collect(Collectors.toList());

        // Đảo ngược danh sách để hiển thị thông báo mới nhất lên đầu
        Collections.reverse(patientNotifications);

        // Khởi tạo Adapter với listener
        adapter = new NotificationAdapter(patientNotifications, notification -> {
            // Xử lý xóa 1 mục
            storageRepository.notifications.remove(notification);
            storageRepository.saveNotifications();
            loadNotifications(); // Tải lại danh sách
        });

        binding.rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvNotifications.setAdapter(adapter);

        updateEmptyView();
    }

    private void showDeleteAllConfirmation() {
        if (getContext() == null) return;
        new AlertDialog.Builder(getContext())
                .setTitle("Xóa tất cả thông báo")
                .setMessage("Bạn có chắc chắn muốn xóa tất cả thông báo?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    int currentPatientId = 1; // TODO: Lấy ID
                    storageRepository.notifications.removeIf(n -> n.getPatientId() == currentPatientId);
                    storageRepository.saveNotifications();
                    loadNotifications();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void updateEmptyView() {
        if (patientNotifications.isEmpty()) {
            binding.tvNoNotifications.setVisibility(View.VISIBLE);
            binding.rvNotifications.setVisibility(View.GONE); // Ẩn RecyclerView
        } else {
            binding.tvNoNotifications.setVisibility(View.GONE);
            binding.rvNotifications.setVisibility(View.VISIBLE); // Hiện RecyclerView
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
