package com.example.medibookandroid.ui.common; // Bạn có thể để trong package common hoặc base

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.medibookandroid.databinding.FragmentSettingsBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class BaseSettingsFragment extends Fragment {

    // Dùng protected để lớp con có thể truy cập nếu cần
    protected FragmentSettingsBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Sử dụng layout chung cho cả 2
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Logic chung: Nút Back trên Toolbar
        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(view).navigateUp());

        // 2. Logic chung: Dialog xóa tài khoản (UI giống hệt nhau)
        binding.itemDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());

        // 3. Gọi hàm trừu tượng để lớp con cài đặt các nút điều hướng riêng
        setupNavigationActions();
    }

    /**
     * Hàm hiển thị Dialog xác nhận xóa tài khoản
     * Logic UI nằm ở đây để không phải viết lại 2 lần
     */
    protected void showDeleteAccountDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận Xóa Tài khoản")
                .setMessage("Hành động này không thể hoàn tác. Bạn có chắc chắn muốn xóa tài khoản?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    // Hiện tại chỉ show Toast, sau này sẽ gọi ViewModel ở đây
                    Toast.makeText(getContext(), "Chức năng Xóa Tài khoản đang phát triển", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // --- LOGIC ẨN/HIỆN BOTTOM NAV ---

    @Override
    public void onResume() {
        super.onResume();
        toggleBottomNav(false); // Ẩn khi vào màn hình này
    }

    @Override
    public void onPause() {
        super.onPause();
        toggleBottomNav(true); // Hiện lại khi thoát ra
    }

    private void toggleBottomNav(boolean isVisible) {
        if (getActivity() != null) {
            // Tìm View dựa trên ID do lớp con cung cấp
            View bottomNav = getActivity().findViewById(getBottomNavigationId());
            if (bottomNav != null) {
                bottomNav.setVisibility(isVisible ? View.VISIBLE : View.GONE);
            }
        }
    }

    // --- CÁC HÀM TRỪU TƯỢNG (ABSTRACT) ---
    // Lớp con BẮT BUỘC phải định nghĩa các hàm này

    /**
     * @return R.id của BottomNavigationView tương ứng với vai trò (Bác sĩ/Bệnh nhân)
     */
    protected abstract int getBottomNavigationId();

    /**
     * Cài đặt sự kiện click cho các mục: Thông báo, Ngôn ngữ, Mật khẩu...
     * Vì Action ID trong Navigation Graph khác nhau nên lớp con phải tự định nghĩa.
     */
    protected abstract void setupNavigationActions();

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}