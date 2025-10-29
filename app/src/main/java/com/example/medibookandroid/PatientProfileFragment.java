package com.example.medibookandroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast; // Import Toast for demo purposes

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

// GIẢ SỬ BẠN ĐÃ ĐỔI TÊN FILE LAYOUT VÀ BINDING TƯƠNG ỨNG
// Nếu không, hãy thay đổi tên Binding ở đây cho đúng
import com.example.medibookandroid.databinding.FragmentPatientProfileBinding;
import com.example.medibookandroid.model.StorageRepository;

public class PatientProfileFragment extends Fragment {

    // Sử dụng Binding cho layout mới
    private FragmentPatientProfileBinding binding;
    private StorageRepository storageRepository;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate layout mới
        binding = FragmentPatientProfileBinding.inflate(inflater, container, false);
        storageRepository = StorageRepository.getInstance(getContext());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final NavController navController = Navigation.findNavController(view);

        // --- CẬP NHẬT LOGIC ---

        // 1. Chỉ cần đặt tên người dùng (lấy từ dữ liệu thật sau này)
        binding.tvUserName.setText("Jane Doe"); // Giữ lại phần demo này

        // 2. Xử lý click cho item "Hồ sơ" -> Chuyển đến màn hình chỉnh sửa
        binding.itemProfile.setOnClickListener(v -> {
            // Sử dụng action ID cũ (nếu bạn chưa đổi) để điều hướng
            navController.navigate(R.id.action_patientProfileFragment_to_patientEditProfileFragment);
            // Hoặc tạo action mới nếu cần
        });

        // --- CẬP NHẬT: XỬ LÝ CLICK CHO ITEM "CÀI ĐẶT" ---
        binding.itemSettings.setOnClickListener(v -> {
            // Sử dụng action ID từ file navigation graph để điều hướng
            navController.navigate(R.id.action_patientProfileFragment_to_settingsFragment);
        });

        binding.itemHelp.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Chức năng Trợ giúp chưa được triển khai", Toast.LENGTH_SHORT).show();
            // TODO: Điều hướng đến màn hình Trợ giúp
        });


        // 5. Xử lý click cho item "Đăng xuất" (Giữ logic cũ)
        // --- CẬP NHẬT LOGIC ĐĂNG XUẤT ---
        binding.itemLogout.setOnClickListener(v -> {
            // 2. HIỂN THỊ HỘP THOẠI XÁC NHẬN
            new AlertDialog.Builder(requireContext()) // Use requireContext() for non-null Context
                    .setTitle("Xác nhận Đăng xuất")
                    .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                    .setPositiveButton("Đăng xuất", (dialog, which) -> {
                        // 3. NẾU NGƯỜI DÙNG CHỌN "ĐĂNG XUẤT", THỰC HIỆN LOGIC CŨ
                        storageRepository.logoutUser();
                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        // getActivity().finish(); // Cân nhắc thêm dòng này
                    })
                    .setNegativeButton("Hủy", (dialog, which) -> {
                        // 4. NẾU NGƯỜI DÙNG CHỌN "HỦY", ĐÓNG HỘP THOẠI
                        dialog.dismiss();
                    })
                    .show(); // Hiển thị hộp thoại
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Quan trọng để tránh leak bộ nhớ
    }
}
