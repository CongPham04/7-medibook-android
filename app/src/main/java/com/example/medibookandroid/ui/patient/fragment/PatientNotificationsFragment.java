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
import com.example.medibookandroid.ui.common.LoadingDialog;
import com.example.medibookandroid.ui.patient.viewmodel.NotificationViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

// Implement cả 2 listener
public class PatientNotificationsFragment extends Fragment implements
        NotificationAdapter.OnNotificationDeleteListener,
        NotificationAdapter.OnNotificationClickListener {

    private FragmentPatientNotificationsBinding binding;
    private NotificationViewModel viewModel;
    private NotificationAdapter adapter;
    private String currentPatientId;

    private LoadingDialog loadingDialog;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPatientNotificationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Lấy ViewModel được chia sẻ từ Activity
        viewModel = new ViewModelProvider(requireActivity()).get(NotificationViewModel.class);
        loadingDialog = new LoadingDialog();

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

        // ⭐️ SỬA LỖI LOGIC: Xóa hàm "markAllAsRead()"
        // Bằng cách xóa 2 dòng này, thông báo sẽ KHÔNG tự động
        // bị đánh dấu là đã đọc khi bạn vào màn hình.
        // if (viewModel != null) {
        //     viewModel.markAllAsRead();
        // }
    }

    private void setupRecyclerView() {
        // Truyền `this` cho cả 2 listener
        adapter = new NotificationAdapter(new ArrayList<>(), this, this);
        binding.rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvNotifications.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.tvToolbarDeleteAll.setOnClickListener(v -> {
            if (adapter.getItemCount() > 0) {
                showDeleteAllConfirmation();
            } else {
                Toast.makeText(getContext(), "Không có thông báo để xóa", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupObservers() {
        // 1. Lắng nghe danh sách thông báo
        viewModel.getNotifications().observe(getViewLifecycleOwner(), notifications -> {
            if (notifications != null) {
                adapter.updateData(notifications);
            }
        });

        // 2. Lắng nghe trạng thái TẢI DANH SÁCH (ProgressBar)
        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading == null) return;

            if (isLoading) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.rvNotifications.setVisibility(View.GONE);
                binding.tvNoNotifications.setVisibility(View.GONE);
            } else {
                binding.progressBar.setVisibility(View.GONE);
                if (adapter.getItemCount() == 0) {
                    binding.rvNotifications.setVisibility(View.GONE);
                    binding.tvNoNotifications.setVisibility(View.VISIBLE);
                } else {
                    binding.rvNotifications.setVisibility(View.VISIBLE);
                    binding.tvNoNotifications.setVisibility(View.GONE);
                }
            }
        });

        // 3. Lắng nghe trạng thái XÓA (Dialog)
        viewModel.isDeleting().observe(getViewLifecycleOwner(), isDeleting -> {
            if (isDeleting == null) return;
            if (isDeleting) {
                if (!loadingDialog.isAdded() && getChildFragmentManager() != null) {
                    loadingDialog.show(getChildFragmentManager(), "deleting");
                }
            } else {
                if (loadingDialog.isAdded()) {
                    loadingDialog.dismiss();
                }
            }
        });

        // 4. Lắng nghe thông báo Toast
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
                    viewModel.deleteAllNotifications();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Được gọi khi người dùng nhấn vào MỘT item thông báo (thẻ)
     */
    @Override
    public void onNotificationClick(Notification notification) {
        // Gọi ViewModel để đánh dấu là đã đọc
        viewModel.markNotificationAsRead(notification);

        // (Tùy chọn)
        // Sau này, bạn có thể thêm logic điều hướng ở đây
        // Ví dụ: nếu type == "booking_confirmed", điều hướng đến màn hình Lịch hẹn
    }

    /**
     * Được gọi khi người dùng nhấn vào NÚT XÓA trên item
     */
    @Override
    public void onDeleteClick(Notification notification) {
        // Hiển thị dialog xác nhận xóa
        showDeleteOneConfirmation(notification);
    }
}