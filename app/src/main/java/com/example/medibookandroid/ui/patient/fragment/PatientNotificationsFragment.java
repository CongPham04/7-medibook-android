package com.example.medibookandroid.ui.patient.fragment;

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

import com.example.medibookandroid.ui.adapter.NotificationAdapter;
import com.example.medibookandroid.databinding.FragmentPatientNotificationsBinding;
import com.example.medibookandroid.data.model.Notification;
import com.example.medibookandroid.ui.patient.viewmodel.NotificationViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class PatientNotificationsFragment extends Fragment {

    private FragmentPatientNotificationsBinding binding;
    private NotificationViewModel viewModel;
    private NotificationAdapter adapter;
    private String currentPatientId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPatientNotificationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(NotificationViewModel.class);

        // 1. Lấy UID
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Lỗi xác thực", Toast.LENGTH_SHORT).show();
            return;
        }
        currentPatientId = user.getUid();

        // 2. Setup RecyclerView
        setupRecyclerView();

        // 3. Setup Listeners
        setupListeners();

        // 4. Setup Observers
        setupObservers();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Tải lại dữ liệu khi quay lại màn hình
        loadNotifications();
    }

    private void loadNotifications() {
        if (viewModel != null && currentPatientId != null) {
            viewModel.getNotificationsForUser(currentPatientId);
        }
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(new ArrayList<>(), notification -> {
            // Xử lý xóa 1 mục
            showDeleteOneConfirmation(notification);
        });
        binding.rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvNotifications.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.tvToolbarDeleteAll.setOnClickListener(v -> {
            showDeleteAllConfirmation();
        });
    }

    private void setupObservers() {
        // 1. Lắng nghe danh sách thông báo
        viewModel.getNotificationsForUser(currentPatientId).observe(getViewLifecycleOwner(), notifications -> {
            if (notifications != null) {
                adapter.updateData(notifications);
                updateEmptyView(notifications.isEmpty());
            }
        });

        // 2. Lắng nghe trạng thái Xóa
        viewModel.getDeleteStatus().observe(getViewLifecycleOwner(), success -> {
            if (success == null) return;
            if (Boolean.TRUE.equals(success)) {
                loadNotifications(); // Tải lại danh sách sau khi xóa thành công
            }
        });

        // 3. Lắng nghe thông báo Toast
        viewModel.getToastMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteOneConfirmation(Notification notification) {
        if (getContext() == null) return;
        new AlertDialog.Builder(getContext())
                .setTitle("Xóa thông báo")
                .setMessage("Bạn có chắc muốn xóa thông báo này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    viewModel.deleteNotification(notification.getDocumentId());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showDeleteAllConfirmation() {
        if (getContext() == null) return;
        new AlertDialog.Builder(getContext())
                .setTitle("Xóa tất cả thông báo")
                .setMessage("Bạn có chắc chắn muốn xóa tất cả thông báo?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    viewModel.deleteAllNotifications(currentPatientId);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void updateEmptyView(boolean isEmpty) {
        if (isEmpty) {
            binding.tvNoNotifications.setVisibility(View.VISIBLE);
            binding.rvNotifications.setVisibility(View.GONE);
        } else {
            binding.tvNoNotifications.setVisibility(View.GONE);
            binding.rvNotifications.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
