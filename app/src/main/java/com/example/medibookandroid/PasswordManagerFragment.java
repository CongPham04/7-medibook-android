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
import com.example.medibookandroid.databinding.FragmentPasswordManagerBinding;

public class PasswordManagerFragment extends Fragment {

    private FragmentPasswordManagerBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPasswordManagerBinding.inflate(inflater, container, false);
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

        // Logic demo khi nhấn nút Lưu
        binding.btnChangePassword.setOnClickListener(v -> {
            String oldPassword = binding.tilCurrentPassword.getEditText().getText().toString();
            String newPassword = binding.tilNewPassword.getEditText().getText().toString();
            String confirmPassword = binding.tilConfirmNewPassword.getEditText().getText().toString();

            // Xóa lỗi cũ (nếu có)
            binding.tilCurrentPassword.setError(null);
            binding.tilNewPassword.setError(null);
            binding.tilConfirmNewPassword.setError(null);

            // TODO: Thêm logic kiểm tra mật khẩu cũ và đổi mật khẩu thực tế
            boolean isValid = true; // Biến kiểm tra tạm thời

            if (oldPassword.isEmpty()){
                binding.tilCurrentPassword.setError("Mật khẩu cũ không được để trống");
                isValid = false;
            }
            if (newPassword.isEmpty()) {
                binding.tilNewPassword.setError("Mật khẩu mới không được để trống");
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

            if(isValid) {
                // Giả sử đổi mật khẩu thành công
                showToast("Đã lưu mật khẩu mới (Demo)");
                navController.navigateUp(); // Quay lại màn hình trước
            }
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

