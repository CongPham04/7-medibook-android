package com.example.medibookandroid.ui.patient.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.medibookandroid.R;
import com.example.medibookandroid.databinding.FragmentSettingsBinding;
import com.example.medibookandroid.ui.auth.AuthViewModel;
import com.example.medibookandroid.ui.common.LoadingDialog;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputLayout;

public class PatientSettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private AuthViewModel authViewModel;
    private LoadingDialog loadingDialog;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final NavController navController = Navigation.findNavController(view);

        // 1. Khởi tạo ViewModel và LoadingDialog
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        loadingDialog = new LoadingDialog();

        // 2. Thiết lập lắng nghe kết quả từ ViewModel
        setupObservers(navController);

        // Xử lý nút quay lại
        binding.toolbar.setNavigationOnClickListener(v -> navController.navigateUp());

        // Các nút điều hướng con (Notifications, Language, Password...)
        binding.itemNotificationSetting.setOnClickListener(v -> {
            navController.navigate(R.id.action_settingsFragment_to_notificationSettingsFragment);
        });

        binding.itemLanguageSetting.setOnClickListener(v -> {
            navController.navigate(R.id.action_settingsFragment_to_languageSettingsFragment);
        });

        binding.itemPasswordManager.setOnClickListener(v -> {
            navController.navigate(R.id.action_settingsFragment_to_passwordManagerFragment);
        });

        // 3. Xử lý nút Xóa tài khoản
        binding.itemDeleteAccount.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    /**
     * Hiển thị Dialog yêu cầu nhập mật khẩu để xác nhận xóa
     */
    private void showDeleteConfirmationDialog() {
        if (getContext() == null) return;

        // Inflate layout custom chứa ô nhập mật khẩu
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_confirm_delete, null);
        TextInputLayout tilPassword = dialogView.findViewById(R.id.til_confirm_password);

        // Tạo AlertDialog
        // 1. Tạo một TextView để làm tiêu đề tùy chỉnh
        TextView titleView = new TextView(requireContext());
        titleView.setText("Xóa Tài khoản!");
        titleView.setPadding(50, 40, 50, 10); // Căn lề cho đẹp (Left, Top, Right, Bottom)
        titleView.setTextSize(20); // Kích thước chữ
        titleView.setTypeface(null, android.graphics.Typeface.BOLD); // In đậm
        titleView.setTextColor(android.graphics.Color.RED); // ⭐️ Đặt màu ĐỎ
        // Hoặc màu vàng: titleView.setTextColor(android.graphics.Color.parseColor("#FFC107"));

        final AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setCustomTitle(titleView) // ⭐️ Dùng setCustomTitle thay vì setTitle
                .setView(dialogView)
                .setPositiveButton("Xóa vĩnh viễn", null)
                .setNegativeButton("Hủy", (d, w) -> d.dismiss())
                .create();

        dialog.show();

        // Override sự kiện nút Positive để kiểm tra input trước khi đóng dialog
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String password = tilPassword.getEditText().getText().toString().trim();
            if (password.isEmpty()) {
                tilPassword.setError("Vui lòng nhập mật khẩu để xác nhận");
            } else {
                dialog.dismiss();
                // Gọi ViewModel xóa tài khoản (Role: patient)
                authViewModel.deleteAccount(password, "patient");
            }
        });
    }

    private void setupObservers(NavController navController) {
        // Observer Loading
        authViewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                if (!loadingDialog.isAdded()) loadingDialog.show(getChildFragmentManager(), "loading");
            } else {
                if (loadingDialog.isAdded()) loadingDialog.dismiss();
            }
        });

        // Observer Thành công
        authViewModel.getDeleteAccountSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Toast.makeText(getContext(), "Tài khoản của bạn đã được xóa.", Toast.LENGTH_LONG).show();
                // Điều hướng về màn hình Welcome/Login
                try {
                    // Xóa toàn bộ backstack và về màn hình Welcome
                    navController.popBackStack(R.id.welcomeFragment, false);
                } catch (Exception e) {
                    // Fallback nếu không tìm thấy ID
                    if (getActivity() != null) getActivity().finish();
                }
            }
        });

        // Observer Lỗi
        authViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });
    }

    // Ẩn BottomNav khi vào màn hình này
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

    // Hiện lại BottomNav khi thoát màn hình này
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
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}