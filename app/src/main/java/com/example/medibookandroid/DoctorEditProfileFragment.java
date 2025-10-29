package com.example.medibookandroid;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

// THAY ĐỔI: Sử dụng Binding mới
import com.example.medibookandroid.databinding.FragmentDoctorEditProfileBinding;
import com.example.medibookandroid.model.Doctor;
import android.content.Intent;
import com.example.medibookandroid.model.StorageRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;

// THAY ĐỔI: Đổi tên class
public class DoctorEditProfileFragment extends Fragment {

    // THAY ĐỔI: Sử dụng Binding mới
    private FragmentDoctorEditProfileBinding binding;
    private StorageRepository storageRepository;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // THAY ĐỔI: Sử dụng Binding mới
        binding = FragmentDoctorEditProfileBinding.inflate(inflater, container, false);
        storageRepository = StorageRepository.getInstance(getContext());
        return binding.getRoot();
    }

    // --- THÊM MỚI: Ẩn/Hiện BottomNav ---
    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            // Giả sử ID của BottomNav bác sĩ là 'doctor_bottom_nav'
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
    // --- KẾT THÚC THÊM MỚI ---

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final NavController navController = Navigation.findNavController(view);

        // Giả sử bác sĩ đã đăng nhập là người đầu tiên
        Doctor currentDoctor = storageRepository.doctors.get(0);
        if (currentDoctor != null) {
            binding.tilFullName.getEditText().setText(currentDoctor.getName());
            binding.tilSpecialty.getEditText().setText(currentDoctor.getSpecialty());
            binding.tilQualification.getEditText().setText(currentDoctor.getQualifications());
            binding.tilWorkplace.getEditText().setText(currentDoctor.getWorkplace());
            binding.tilPhone.getEditText().setText(currentDoctor.getPhone());
            binding.tilDescription.getEditText().setText(currentDoctor.getDescription());
        }

        binding.btnSaveProfile.setOnClickListener(v -> {
            // TODO: Logic lưu thông tin đã cập nhật vào StorageRepository
            Toast.makeText(getContext(), "Hồ sơ đã được cập nhật", Toast.LENGTH_SHORT).show();
            navController.popBackStack(); // Quay lại màn hình Profile menu
        });

        // THÊM MỚI: Xử lý nút quay lại trên toolbar
        binding.toolbar.setNavigationOnClickListener(v -> navController.popBackStack());

        // ĐÃ XÓA NÚT ĐĂNG XUẤT (chuyển sang Profile menu)
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
