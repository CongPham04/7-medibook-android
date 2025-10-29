package com.example.medibookandroid;

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

import com.example.medibookandroid.databinding.FragmentDoctorProfileBinding;
import com.example.medibookandroid.model.Doctor;
import com.example.medibookandroid.model.StorageRepository;

public class DoctorProfileFragment extends Fragment {

    private FragmentDoctorProfileBinding binding;
    private StorageRepository storageRepository;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDoctorProfileBinding.inflate(inflater, container, false);
        storageRepository = StorageRepository.getInstance(getContext());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Cần tìm NavController cho doctor_nav_host_fragment
        final NavController navController = Navigation.findNavController(requireActivity(), R.id.doctor_nav_host_fragment);

        // Giả sử bác sĩ đã đăng nhập là người đầu tiên
        Doctor currentDoctor = storageRepository.doctors.get(0);
        binding.tvUserName.setText(currentDoctor.getName());

        // Xử lý click cho item "Hồ sơ" -> Chuyển đến màn hình chỉnh sửa
        binding.itemProfile.setOnClickListener(v -> {
            // Điều hướng đến trang Chỉnh sửa (đã đổi tên)
            navController.navigate(R.id.action_doctorProfileFragment_to_doctorEditProfileFragment);
        });

        // Xử lý click cho item "Cài đặt"
        binding.itemSettings.setOnClickListener(v -> {
            // Điều hướng đến Cài đặt
            navController.navigate(R.id.action_doctorProfileFragment_to_doctorSettingsFragment);
        });

        binding.itemHelp.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Chức năng Trợ giúp chưa được triển khai", Toast.LENGTH_SHORT).show();
        });

        binding.itemLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Xác nhận Đăng xuất")
                    .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                    .setPositiveButton("Đăng xuất", (dialog, which) -> {
                        storageRepository.logoutUser();
                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    })
                    .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
