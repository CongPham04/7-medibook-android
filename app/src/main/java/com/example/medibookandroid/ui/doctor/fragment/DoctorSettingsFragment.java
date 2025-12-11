package com.example.medibookandroid.ui.doctor.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.medibookandroid.R;
import com.example.medibookandroid.databinding.FragmentDoctorSettingsBinding;
import com.example.medibookandroid.ui.auth.AuthViewModel;
import com.example.medibookandroid.ui.common.LoadingDialog;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputLayout;

public class DoctorSettingsFragment extends Fragment {

    private FragmentDoctorSettingsBinding binding;
    private AuthViewModel authViewModel;
    private LoadingDialog loadingDialog;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDoctorSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final NavController navController = Navigation.findNavController(view);

        // 1. Khởi tạo ViewModel và Dialog
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        loadingDialog = new LoadingDialog();

        // 2. Thiết lập lắng nghe kết quả
        setupObservers(navController);

        binding.toolbar.setNavigationOnClickListener(v -> navController.navigateUp());

        // Các nút điều hướng con
        binding.itemNotificationSetting.setOnClickListener(v -> {
            navController.navigate(R.id.action_doctorSettingsFragment_to_doctorNotificationSettingsFragment);
        });

        binding.itemLanguageSetting.setOnClickListener(v -> {
            navController.navigate(R.id.action_doctorSettingsFragment_to_doctorLanguageSettingsFragment);
        });

        binding.itemPasswordManager.setOnClickListener(v -> {
            navController.navigate(R.id.action_doctorSettingsFragment_to_doctorPasswordManagerFragment);
        });

        // 3. Xử lý nút Xóa tài khoản
        binding.itemDeleteAccount.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void showDeleteConfirmationDialog() {
        if (getContext() == null) return;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_confirm_delete, null);
        TextInputLayout tilPassword = dialogView.findViewById(R.id.til_confirm_password);

        final AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("CẢNH BÁO: Xóa Tài khoản Bác sĩ")
                .setView(dialogView)
                .setPositiveButton("Xóa vĩnh viễn", null)
                .setNegativeButton("Hủy", (d, w) -> d.dismiss())
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String password = tilPassword.getEditText().getText().toString().trim();
            if (password.isEmpty()) {
                tilPassword.setError("Vui lòng nhập mật khẩu");
            } else {
                dialog.dismiss();
                // Gọi ViewModel xóa tài khoản (Role: doctor)
                authViewModel.deleteAccount(password, "doctor");
            }
        });
    }

    private void setupObservers(NavController navController) {
        authViewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                if (!loadingDialog.isAdded()) loadingDialog.show(getChildFragmentManager(), "loading");
            } else {
                if (loadingDialog.isAdded()) loadingDialog.dismiss();
            }
        });

        authViewModel.getDeleteAccountSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Toast.makeText(getContext(), "Tài khoản bác sĩ đã được xóa.", Toast.LENGTH_LONG).show();
                try {
                    navController.popBackStack(R.id.welcomeFragment, false);
                } catch (Exception e) {
                    if (getActivity() != null) getActivity().finish();
                }
            }
        });

        authViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.doctor_bottom_nav);
            if (bottomNav != null) {
                bottomNav.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() != null) {
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.doctor_bottom_nav);
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