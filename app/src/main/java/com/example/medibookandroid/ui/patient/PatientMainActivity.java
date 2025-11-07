package com.example.medibookandroid.ui.patient;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider; // ⭐️ THÊM
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.example.medibookandroid.R;
import com.example.medibookandroid.databinding.ActivityPatientMainBinding;
import com.example.medibookandroid.ui.patient.viewmodel.NotificationViewModel; // ⭐️ THÊM
import com.google.android.material.badge.BadgeDrawable; // ⭐️ THÊM

public class PatientMainActivity extends AppCompatActivity {

    private ActivityPatientMainBinding binding;
    private NotificationViewModel notificationViewModel; // ⭐️ THÊM

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPatientMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ⭐️ THÊM: Khởi tạo ViewModel (Activity-scoped)
        // ViewModel này sẽ tồn tại suốt vòng đời của Activity
        // và được chia sẻ cho các Fragment
        notificationViewModel = new ViewModelProvider(this).get(NotificationViewModel.class);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.patient_nav_host_fragment);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();

            // 1. Liên kết Menu với NavController
            NavigationUI.setupWithNavController(binding.patientBottomNav, navController);

            // 2. Xử lý khi click LẠI item
            binding.patientBottomNav.setOnItemReselectedListener(item -> {
                int itemId = item.getItemId();
                int currentDestId = navController.getCurrentDestination() != null ? navController.getCurrentDestination().getId() : 0;

                // Nếu nhấn lại tab "Trang chủ"
                if (itemId == R.id.patientHomeFragment) {
                    // Và nếu bạn KHÔNG ở Home (ví dụ: đang ở Settings)
                    if (currentDestId != R.id.patientHomeFragment) {
                        navController.popBackStack(R.id.patientHomeFragment, false);
                    }
                }
                // Nếu nhấn lại tab "Lịch hẹn"
                else if (itemId == R.id.patientAppointmentsFragment) {
                    if (currentDestId != R.id.patientAppointmentsFragment) {
                        navController.popBackStack(R.id.patientAppointmentsFragment, false);
                    }
                }
                // Nếu nhấn lại tab "Thông báo"
                else if (itemId == R.id.patientNotificationsFragment) {
                    if (currentDestId != R.id.patientNotificationsFragment) {
                        navController.popBackStack(R.id.patientNotificationsFragment, false);
                    }
                }
                // Nếu nhấn lại tab "Hồ sơ" (ví dụ: đang ở "Sửa hồ sơ")
                else if (itemId == R.id.patientProfileFragment) {
                    if (currentDestId != R.id.patientProfileFragment) {
                        navController.popBackStack(R.id.patientProfileFragment, false);
                    }
                }
            });

            // 3. ⭐️ THÊM: Quan sát số lượng thông báo chưa đọc
            setupNotificationBadge();

        } else {
            Log.e("PatientMainActivity", "NavHostFragment không được tìm thấy!");
        }
    }

    // ⭐️ THÊM HÀM MỚI ⭐️
    private void setupNotificationBadge() {
        notificationViewModel.getUnreadCount().observe(this, count -> {
            if (count == null) return;

            // Lấy badge của tab "Notifications"
            BadgeDrawable badge = binding.patientBottomNav.getOrCreateBadge(R.id.patientNotificationsFragment);

            if (count > 0) {
                badge.setVisible(true);
                badge.setNumber(count);
            } else {
                badge.setVisible(false);
                badge.clearNumber();
            }
        });
    }
}