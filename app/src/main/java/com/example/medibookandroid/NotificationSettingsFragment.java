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

// 1. IMPORT BottomNavigationView
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.medibookandroid.databinding.FragmentNotificationSettingsBinding;

public class NotificationSettingsFragment extends Fragment {

    private FragmentNotificationSettingsBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentNotificationSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    // 2. THÊM onResume ĐỂ ẨN BOTTOM NAV
    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.patient_bottom_nav); // Đảm bảo ID này đúng
            if (bottomNav != null) {
                bottomNav.setVisibility(View.GONE);
            }
        }
    }

    // 3. THÊM onPause ĐỂ HIỆN LẠI BOTTOM NAV
    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() != null) {
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.patient_bottom_nav); // Đảm bảo ID này đúng
            if (bottomNav != null) {
                bottomNav.setVisibility(View.VISIBLE);
            }
        }
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final NavController navController = Navigation.findNavController(view);

        // Xử lý nút quay lại trên Toolbar
        binding.toolbar.setNavigationOnClickListener(v -> navController.navigateUp());

        // Logic demo khi thay đổi trạng thái Switch
        binding.switchVibrate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            showToast("Thông báo xác nhận lịch hẹn: " + (isChecked ? "Bật" : "Tắt"));
            // TODO: Lưu trạng thái cài đặt này
        });

        binding.switchSound.setOnCheckedChangeListener((buttonView, isChecked) -> {
            showToast("Thông báo nhắc lịch hẹn: " + (isChecked ? "Bật" : "Tắt"));
            // TODO: Lưu trạng thái cài đặt này
        });

        binding.switchGeneralNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            showToast("Thông báo cập nhật chung: " + (isChecked ? "Bật" : "Tắt"));
            // TODO: Lưu trạng thái cài đặt này
        });
    }

    private void showToast(String message) {
        if(getContext() != null) { // Thêm kiểm tra null cho context
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

