package com.example.medibookandroid.ui.doctor;

import android.os.Bundle;
import android.util.Log; // ⭐️ THÊM
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider; // ⭐️ THÊM
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.example.medibookandroid.R;
import com.example.medibookandroid.databinding.ActivityDoctorMainBinding;
import com.example.medibookandroid.ui.doctor.viewmodel.DoctorRequestsViewModel; // ⭐️ THÊM
import com.google.android.material.badge.BadgeDrawable; // ⭐️ THÊM

public class DoctorMainActivity extends AppCompatActivity {

    private ActivityDoctorMainBinding binding;
    private DoctorRequestsViewModel requestsViewModel; // ⭐️ THÊM

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDoctorMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ⭐️ THÊM: Khởi tạo ViewModel (Activity-scoped)
        requestsViewModel = new ViewModelProvider(this).get(DoctorRequestsViewModel.class);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.doctor_nav_host_fragment);

        // ⭐️ SỬA: Thêm kiểm tra null
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(binding.doctorBottomNav, navController);

            // ⭐️ THÊM: Logic xử lý "nhấn lại" tab
            binding.doctorBottomNav.setOnItemReselectedListener(item -> {
                int itemId = item.getItemId();
                int currentDestId = navController.getCurrentDestination() != null ? navController.getCurrentDestination().getId() : 0;

                if (itemId == R.id.doctorScheduleFragment && currentDestId != R.id.doctorScheduleFragment) {
                    navController.popBackStack(R.id.doctorScheduleFragment, false);
                }
                else if (itemId == R.id.doctorRequestsFragment && currentDestId != R.id.doctorRequestsFragment) {
                    navController.popBackStack(R.id.doctorRequestsFragment, false);
                }
                else if (itemId == R.id.doctorProfileFragment && currentDestId != R.id.doctorProfileFragment) {
                    navController.popBackStack(R.id.doctorProfileFragment, false);
                }
            });

            // ⭐️ THÊM: Logic cập nhật Badge
            setupRequestsBadge();

        } else {
            Log.e("DoctorMainActivity", "NavHostFragment không được tìm thấy!");
        }
    }

    // ⭐️ THÊM HÀM MỚI ⭐️
    /**
     * Lắng nghe danh sách yêu cầu (pending) và cập nhật Badge
     */
    private void setupRequestsBadge() {
        requestsViewModel.getPendingRequests().observe(this, pendingList -> {
            // Lấy badge của tab "Yêu cầu"
            BadgeDrawable badge = binding.doctorBottomNav.getOrCreateBadge(R.id.doctorRequestsFragment);

            if (pendingList != null && !pendingList.isEmpty()) {
                badge.setVisible(true);
                badge.setNumber(pendingList.size());
            } else {
                badge.setVisible(false);
                badge.clearNumber();
            }
        });
    }
}