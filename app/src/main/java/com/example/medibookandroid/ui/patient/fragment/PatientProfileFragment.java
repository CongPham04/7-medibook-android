package com.example.medibookandroid.ui.patient.fragment; // ⭐️ SỬA PACKAGE NẾU CẦN

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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.medibookandroid.MainActivity;
import com.example.medibookandroid.R;
import com.example.medibookandroid.data.local.SharedPrefHelper;
import com.example.medibookandroid.databinding.FragmentUserProfileBinding;
import com.example.medibookandroid.ui.auth.AuthViewModel;
import com.example.medibookandroid.ui.patient.viewmodel.PatientViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PatientProfileFragment extends Fragment {

    private FragmentUserProfileBinding binding;
    // ⭐️ SỬA: Dùng ViewModels
    private PatientViewModel patientViewModel;
    private AuthViewModel authViewModel;

    private NavController navController;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentUserProfileBinding.inflate(inflater, container, false);
        // ⭐️ XÓA: StorageRepository
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        // 1. Khởi tạo ViewModels
        patientViewModel = new ViewModelProvider(this).get(PatientViewModel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // 2. Tải dữ liệu hồ sơ
        loadProfileData();

        // 3. Quan sát dữ liệu
        setupObservers();

        // 4. Cài đặt các nút
        setupClickListeners();
    }

    /**
     * Lấy UID của người dùng hiện tại và yêu cầu ViewModel tải dữ liệu
     */
    private void loadProfileData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String patientId = currentUser.getUid();
            patientViewModel.loadPatient(patientId);
        } else {
            // Trường hợp lỗi (ví dụ: mất phiên đăng nhập)
            Toast.makeText(getContext(), "Lỗi xác thực, đang đăng xuất...", Toast.LENGTH_SHORT).show();
            logout(); // Gọi hàm đăng xuất
        }
    }

    /**
     * Lắng nghe LiveData từ ViewModel
     */
    private void setupObservers() {
        patientViewModel.getPatient().observe(getViewLifecycleOwner(), patient -> {
            if (patient != null) {
                // ⭐️ SỬA: Hiển thị dữ liệu thật
                binding.tvUserName.setText(patient.getFullName());

                // Tải Avatar bằng Glide
                if (patient.getAvatarUrl() != null && !patient.getAvatarUrl().isEmpty() && getContext() != null) {
                    Glide.with(getContext())
                            .load(patient.getAvatarUrl())
                            .placeholder(R.drawable.logo2) // Ảnh mặc định
                            .circleCrop() // Bo tròn ảnh
                            .into(binding.ivUserAvatar); // ⭐️ Phải có ID 'ivUserAvatar' trong XML
                }
            } else {
                // Xử lý trường hợp không tìm thấy hồ sơ (dù đã đăng nhập)
                binding.tvUserName.setText("Không tìm thấy hồ sơ");
            }
        });
    }

    /**
     * Cài đặt các sự kiện click
     */
    private void setupClickListeners() {
        binding.itemProfile.setOnClickListener(v -> {
            navController.navigate(R.id.action_patientProfileFragment_to_patientEditProfileFragment);
        });

        binding.itemSettings.setOnClickListener(v -> {
            navController.navigate(R.id.action_patientProfileFragment_to_settingsFragment);
        });

        binding.itemHelp.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Chức năng Trợ giúp chưa được triển khai", Toast.LENGTH_SHORT).show();
        });

        binding.itemLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Xác nhận Đăng xuất")
                    .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                    .setPositiveButton("Đăng xuất", (dialog, which) -> {
                        // ⭐️ SỬA: Gọi hàm logout
                        logout();
                    })
                    .setNegativeButton("Hủy", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        });
    }

    /**
     * Xử lý logic đăng xuất và quay về màn hình chính
     */
    private void logout() {
        // 1. Xóa SharedPreferences
        SharedPrefHelper prefHelper = new SharedPrefHelper(requireContext());
        prefHelper.clear(); // Hoặc prefHelper.remove("user_role");

        // 2. Gọi AuthViewModel để đăng xuất khỏi FirebaseAuth
        authViewModel.logout();

        // 3. Điều hướng về MainActivity, xóa hết stack cũ
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
