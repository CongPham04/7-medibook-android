package com.example.medibookandroid.ui.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.medibookandroid.databinding.FragmentPasswordManagerBinding;

public abstract class BasePasswordManagerFragment extends Fragment {

    protected FragmentPasswordManagerBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Dùng chung layout XML cũ của bạn
        binding = FragmentPasswordManagerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Xử lý nút Back
        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(view).navigateUp());

        // 2. Xử lý sự kiện đổi mật khẩu
        binding.btnChangePassword.setOnClickListener(v -> handlePasswordChange());
    }

    /**
     * Hàm xử lý logic kiểm tra và gọi API đổi mật khẩu
     */
    private void handlePasswordChange() {
        String oldPassword = "";
        if (binding.tilCurrentPassword.getEditText() != null) {
            oldPassword = binding.tilCurrentPassword.getEditText().getText().toString().trim();
        }

        String newPassword = "";
        if (binding.tilNewPassword.getEditText() != null) {
            newPassword = binding.tilNewPassword.getEditText().getText().toString().trim();
        }

        String confirmPassword = "";
        if (binding.tilConfirmNewPassword.getEditText() != null) {
            confirmPassword = binding.tilConfirmNewPassword.getEditText().getText().toString().trim();
        }

        // Xóa lỗi cũ
        binding.tilCurrentPassword.setError(null);
        binding.tilNewPassword.setError(null);
        binding.tilConfirmNewPassword.setError(null);

        boolean isValid = true;

        // Validation Logic
        if (oldPassword.isEmpty()) {
            binding.tilCurrentPassword.setError("Mật khẩu cũ không được để trống");
            isValid = false;
        }
        if (newPassword.isEmpty()) {
            binding.tilNewPassword.setError("Mật khẩu mới không được để trống");
            isValid = false;
        } else if (newPassword.length() < 6) {
            binding.tilNewPassword.setError("Mật khẩu mới phải có ít nhất 6 ký tự");
            isValid = false;
        }

        if (confirmPassword.isEmpty()) {
            binding.tilConfirmNewPassword.setError("Xác nhận mật khẩu không được để trống");
            isValid = false;
        }

        if (!newPassword.equals(confirmPassword) && !newPassword.isEmpty() && !confirmPassword.isEmpty()) {
            binding.tilConfirmNewPassword.setError("Mật khẩu xác nhận không khớp");
            isValid = false;
        }

        if (isValid) {
            // TODO: Gọi AuthViewModel để thực hiện đổi mật khẩu trên Firebase
            showToast("Đã lưu mật khẩu mới (Demo)");
            Navigation.findNavController(binding.getRoot()).navigateUp();
        }
    }

    // --- LOGIC ẨN/HIỆN BOTTOM NAV ---

    @Override
    public void onResume() {
        super.onResume();
        toggleBottomNav(false); // Ẩn Nav khi vào màn hình này
    }

    @Override
    public void onPause() {
        super.onPause();
        toggleBottomNav(true); // Hiện Nav khi thoát ra
    }

    private void toggleBottomNav(boolean isVisible) {
        if (getActivity() != null) {
            // Gọi hàm abstract getBottomNavigationId() để lấy ID đúng
            View bottomNav = getActivity().findViewById(getBottomNavigationId());
            if (bottomNav != null) {
                bottomNav.setVisibility(isVisible ? View.VISIBLE : View.GONE);
            }
        }
    }

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    // --- HÀM TRỪU TƯỢNG ---
    // Lớp con bắt buộc phải cung cấp ID của BottomNav
    protected abstract int getBottomNavigationId();

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}