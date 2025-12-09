package com.example.medibookandroid.ui.patient.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

// 1. IMPORT BottomNavigationView
import com.example.medibookandroid.R;
import com.example.medibookandroid.ui.help.HelpActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.medibookandroid.databinding.FragmentSettingsBinding; // Đảm bảo tên Binding đúng

public class PatientSettingsFragment extends Fragment {

    private FragmentSettingsBinding binding; // Sử dụng ViewBinding

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
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

        // Xử lý click cho từng item
        binding.itemNotificationSetting.setOnClickListener(v -> {
            navController.navigate(R.id.action_settingsFragment_to_notificationSettingsFragment);
        });

        binding.itemLanguageSetting.setOnClickListener(v -> {
            navController.navigate(R.id.action_settingsFragment_to_languageSettingsFragment);
        });

        binding.itemPasswordManager.setOnClickListener(v -> {
            navController.navigate(R.id.action_settingsFragment_to_passwordManagerFragment);
        });

        binding.itemDeleteAccount.setOnClickListener(v -> {
            // Hiển thị hộp thoại xác nhận xóa tài khoản
            new AlertDialog.Builder(requireContext())
                    .setTitle("Xác nhận Xóa Tài khoản")
                    .setMessage("Hành động này không thể hoàn tác. Bạn có chắc chắn muốn xóa tài khoản?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        // TODO: Thêm logic xóa tài khoản thực tế ở đây
                        Toast.makeText(getContext(), "Chức năng Xóa Tài khoản đang phát triển", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                    .show();
        });
        // Ví dụ trong PatientSettingsFragment.java
        // THÊM DÒNG NÀY – MỞ MÀN HÌNH TRỢ GIÚP
        // NÚT TRỢ GIÚP TRÊN MÀN HÌNH HỒ SƠ
        binding.itemHelp.setOnClickListener(v ->
                startActivity(new Intent(requireActivity(), com.example.medibookandroid.ui.help.HelpActivity.class))
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Quan trọng để tránh leak bộ nhớ
    }
}