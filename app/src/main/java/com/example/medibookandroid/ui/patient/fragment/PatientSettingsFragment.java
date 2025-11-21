package com.example.medibookandroid.ui.patient.fragment;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.example.medibookandroid.R;
import com.example.medibookandroid.ui.common.BaseSettingsFragment;

public class PatientSettingsFragment extends BaseSettingsFragment {

    @Override
    protected int getBottomNavigationId() {
        // Trả về ID Bottom Nav của Bệnh nhân
        return R.id.patient_bottom_nav;
    }

    @Override
    protected void setupNavigationActions() {
        NavController navController = Navigation.findNavController(binding.getRoot());

        // Cài đặt điều hướng sử dụng Action ID của Bệnh nhân
        // (Lưu ý: Tên action có thể khác tùy vào file navigation graph của bạn)

        binding.itemNotificationSetting.setOnClickListener(v -> {
            navController.navigate(R.id.action_settingsFragment_to_notificationSettingsFragment);
        });

        binding.itemLanguageSetting.setOnClickListener(v -> {
            navController.navigate(R.id.action_settingsFragment_to_languageSettingsFragment);
        });

        binding.itemPasswordManager.setOnClickListener(v -> {
            navController.navigate(R.id.action_settingsFragment_to_passwordManagerFragment);
        });
    }
}