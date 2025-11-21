package com.example.medibookandroid.ui.doctor.fragment;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.example.medibookandroid.R;
import com.example.medibookandroid.ui.common.BaseSettingsFragment;

public class DoctorSettingsFragment extends BaseSettingsFragment {

    @Override
    protected int getBottomNavigationId() {
        // Trả về ID Bottom Nav của Bác sĩ
        return R.id.doctor_bottom_nav;
    }

    @Override
    protected void setupNavigationActions() {
        // Lấy NavController
        // Lưu ý: binding đã có sẵn từ lớp cha (BaseSettingsFragment)
        NavController navController = Navigation.findNavController(binding.getRoot());

        // Cài đặt điều hướng sử dụng Action ID của Bác sĩ

        binding.itemNotificationSetting.setOnClickListener(v -> {
            navController.navigate(R.id.action_doctorSettingsFragment_to_notificationSettingsFragment);
        });

        binding.itemLanguageSetting.setOnClickListener(v -> {
            navController.navigate(R.id.action_doctorSettingsFragment_to_doctorLanguageSettingsFragment);
        });

        binding.itemPasswordManager.setOnClickListener(v -> {
            navController.navigate(R.id.action_doctorSettingsFragment_to_doctorPasswordManagerFragment);
        });
    }
}