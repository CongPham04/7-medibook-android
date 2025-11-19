package com.example.medibookandroid.ui.doctor.fragment; // ⭐️ SỬA PACKAGE NẾU CẦN

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
import androidx.lifecycle.ViewModelProvider; // ⭐️ THÊM
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide; // ⭐️ THÊM
import com.example.medibookandroid.MainActivity; // ⭐️ THÊM
import com.example.medibookandroid.R; // ⭐️ THÊM
import com.example.medibookandroid.databinding.FragmentDoctorProfileBinding;
// ⭐️ SỬA IMPORTS
import com.example.medibookandroid.ui.auth.AuthViewModel;
import com.example.medibookandroid.ui.doctor.viewmodel.DoctorViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DoctorProfileFragment extends Fragment {

    private FragmentDoctorProfileBinding binding;

    // ⭐️ SỬA: Dùng ViewModels
    private DoctorViewModel doctorViewModel;
    private AuthViewModel authViewModel;

    private NavController navController;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDoctorProfileBinding.inflate(inflater, container, false);
        // ⭐️ XÓA: StorageRepository
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            // Thử tìm NavController của Activity host
            navController = Navigation.findNavController(requireActivity(), R.id.doctor_nav_host_fragment);
        } catch (Exception e) {
            // Fallback nếu không tìm thấy (an toàn)
            navController = Navigation.findNavController(view);
        }

        // 1. Khởi tạo ViewModels
        doctorViewModel = new ViewModelProvider(this).get(DoctorViewModel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // 2. Tải dữ liệu
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
            String doctorId = currentUser.getUid();
            doctorViewModel.loadDoctor(doctorId);
        } else {
            Toast.makeText(getContext(), "Lỗi xác thực, đang đăng xuất...", Toast.LENGTH_SHORT).show();
            logout(); // Gọi hàm đăng xuất
        }
    }

    /**
     * Lắng nghe LiveData từ ViewModel
     */
    private void setupObservers() {
        doctorViewModel.getDoctor().observe(getViewLifecycleOwner(), doctor -> {
            if (doctor != null) {
                // Hiển thị dữ liệu thật
                binding.tvUserName.setText(doctor.getFullName());

                // Tải Avatar bằng Glide (Layout phải có id iv_user_avatar)
                if (doctor.getAvatarUrl() != null && !doctor.getAvatarUrl().isEmpty() && getContext() != null) {
                    Glide.with(getContext())
                            .load(doctor.getAvatarUrl())
                            .placeholder(R.drawable.logo2) // Ảnh mặc định
                            .circleCrop()
                            .into(binding.ivUserAvatar);
                }
            } else {
                binding.tvUserName.setText("Không tìm thấy hồ sơ");
            }
        });
    }

    /**
     * Cài đặt các sự kiện click
     */
    private void setupClickListeners() {
        binding.itemProfile.setOnClickListener(v -> {
            // Điều hướng đến trang Chỉnh sửa
            navController.navigate(R.id.action_doctorProfileFragment_to_doctorEditProfileFragment);
        });

        binding.itemSettings.setOnClickListener(v -> {
//             Điều hướng đến Cài đặt
//             Bạn cần đảm bảo action 'action_doctorProfileFragment_to_doctorSettingsFragment' tồn tại
             navController.navigate(R.id.action_doctorProfileFragment_to_doctorSettingsFragment);
            Toast.makeText(getContext(), "Chuyển sang Cài đặt (Bác sĩ)", Toast.LENGTH_SHORT).show();
        });

        binding.itemHelp.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Chức năng Trợ giúp chưa được triển khai", Toast.LENGTH_SHORT).show();
        });

        binding.itemLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Xác nhận Đăng xuất")
                    .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                    .setPositiveButton("Đăng xuất", (dialog, which) -> {
                        // ⭐️ SỬA: Gọi hàm logout helper
                        logout();
                    })
                    .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }

    /**
     * Xử lý logic đăng xuất và quay về màn hình chính
     */
    private void logout() {
        // 1. Gọi AuthViewModel để đăng xuất khỏi FirebaseAuth
        authViewModel.logout();

        // 2. Điều hướng về MainActivity, xóa hết stack cũ
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

