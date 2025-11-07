package com.example.medibookandroid.ui.patient.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.medibookandroid.R;
import com.example.medibookandroid.data.model.Patient;
import com.example.medibookandroid.databinding.FragmentPatientEditProfileBinding;
import com.example.medibookandroid.ui.patient.viewmodel.PatientViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PatientEditProfileFragment extends Fragment {

    private FragmentPatientEditProfileBinding binding;
    private PatientViewModel viewModel;
    private NavController navController;
    private Patient currentPatientData; // Để giữ object patient

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPatientEditProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    // (Các hàm onResume, onPause giữ nguyên)
    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.patient_bottom_nav);
            if (bottomNav != null) {
                bottomNav.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() != null) {
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.patient_bottom_nav);
            if (bottomNav != null) {
                bottomNav.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        viewModel = new ViewModelProvider(this).get(PatientViewModel.class);

        // 1. Tải dữ liệu
        loadPatientData();

        // 2. Lắng nghe dữ liệu
        setupObservers();

        // 3. Cài đặt nút
        setupClickListeners();
    }

    private void loadPatientData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            viewModel.loadPatient(currentUser.getUid());
        } else {
            Toast.makeText(getContext(), "Lỗi xác thực người dùng.", Toast.LENGTH_SHORT).show();
            navController.popBackStack();
        }
    }

    private void setupObservers() {
        // 1. Lắng nghe thông tin Patient
        viewModel.getPatient().observe(getViewLifecycleOwner(), patient -> {
            if (patient != null) {
                this.currentPatientData = patient; // Lưu lại object
                populateUi(patient); // Điền thông tin vào form
            }
        });

        // 2. Lắng nghe kết quả LƯU
        viewModel.getUpdatePatientStatus().observe(getViewLifecycleOwner(), success -> {
            if (success == null) return; // Bỏ qua giá trị null (reset)

            binding.btnSaveProfile.setEnabled(true); // Kích hoạt lại nút
            binding.btnSaveProfile.setText("Lưu thay đổi");

            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(getContext(), "Cập nhật hồ sơ thành công", Toast.LENGTH_SHORT).show();
                navController.popBackStack(); // Quay lại
            } else {
                Toast.makeText(getContext(), "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Điền dữ liệu từ Patient object vào các ô EditText
     */
    private void populateUi(Patient patient) {
        if (getContext() != null && patient.getAvatarUrl() != null && !patient.getAvatarUrl().isEmpty()) {
            Glide.with(getContext())
                    .load(patient.getAvatarUrl())
                    .placeholder(R.drawable.logo2)
                    .circleCrop()
                    .into(binding.ivUserAvatar);
        }

        binding.tilFullName.getEditText().setText(patient.getFullName());
        binding.tilDob.getEditText().setText(patient.getDob());
        binding.tilGender.getEditText().setText(patient.getGender());
        binding.tilPhone.getEditText().setText(patient.getPhone());
        binding.tilAddress.getEditText().setText(patient.getAddress());
    }

    private void setupClickListeners() {
        binding.btnSaveProfile.setOnClickListener(v -> {
            // ⭐️ SỬA: Thêm bước kiểm tra (Validate)
            if (validateInput()) {
                performSave();
            }
        });

        binding.ivEditAvatarIcon.setOnClickListener(v -> {
            // TODO: Mở thư viện ảnh/camera
            Toast.makeText(getContext(), "Chức năng đổi avatar chưa được triển khai", Toast.LENGTH_SHORT).show();
        });

        binding.toolbar.setNavigationOnClickListener(v -> navController.popBackStack());
    }

    // ⭐️ BẮT ĐẦU THÊM MỚI: Logic Validate ⭐️
    /**
     * Kiểm tra các trường input
     */
    private boolean validateInput() {
        // Reset lỗi
        binding.tilFullName.setError(null);
        binding.tilDob.setError(null);
        binding.tilGender.setError(null);
        binding.tilPhone.setError(null);
        binding.tilAddress.setError(null);

        boolean valid = true;

        String name = binding.tilFullName.getEditText().getText().toString().trim();
        String dob = binding.tilDob.getEditText().getText().toString().trim();
        String gender = binding.tilGender.getEditText().getText().toString().trim();
        String phone = binding.tilPhone.getEditText().getText().toString().trim();
        String address = binding.tilAddress.getEditText().getText().toString().trim();

        // 1. Kiểm tra Tên (Giống RegisterFragment)
        if (name.isEmpty()) {
            binding.tilFullName.setError("Họ tên không được để trống");
            valid = false;
        } else if (name.length() < 5) {
            binding.tilFullName.setError("Họ tên phải dài ít nhất 5 ký tự");
            valid = false;
        } else if (!Character.isLetter(name.charAt(0)) || !Character.isUpperCase(name.charAt(0))) {
            binding.tilFullName.setError("Họ tên phải bắt đầu bằng một chữ cái viết hoa");
            valid = false;
        }

        // 2. Kiểm tra SĐT (Giống RegisterFragment)
        if (phone.isEmpty()) {
            binding.tilPhone.setError("Số điện thoại không được để trống");
            valid = false;
        } else if (formatPhoneNumber(phone) == null) {
            binding.tilPhone.setError("Số điện thoại không hợp lệ (VD: 0912345678)");
            valid = false;
        }

        // 3. Kiểm tra các trường khác (chỉ cần không trống)
        if (dob.isEmpty()) {
            binding.tilDob.setError("Ngày sinh không được để trống");
            valid = false;
        }
        if (gender.isEmpty()) {
            binding.tilGender.setError("Giới tính không được để trống");
            valid = false;
        }
        if (address.isEmpty()) {
            binding.tilAddress.setError("Địa chỉ không được để trống");
            valid = false;
        }

        return valid;
    }

    /**
     * Hàm chuẩn hóa SĐT (Copy từ RegisterFragment)
     */
    private String formatPhoneNumber(String phone) {
        if (phone == null) return null;
        phone = phone.replaceAll("\\s+", "").replaceAll("[^0-9]", "");
        if (phone.startsWith("0") && phone.length() == 10) return "+84" + phone.substring(1);
        if (phone.startsWith("84") && phone.length() == 11) return "+" + phone;
        if (phone.startsWith("+84") && phone.length() == 12) return phone;
        return null; // Không hợp lệ
    }
    // ⭐️ KẾT THÚC THÊM MỚI ⭐️

    /**
     * Lấy dữ liệu từ form, cập nhật Patient object và gọi ViewModel
     */
    private void performSave() { // ⭐️ SỬA: Đổi tên hàm
        if (currentPatientData == null) {
            Toast.makeText(getContext(), "Đang tải dữ liệu, vui lòng chờ...", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnSaveProfile.setEnabled(false);
        binding.btnSaveProfile.setText("Đang lưu...");

        // Lấy dữ liệu mới từ Form
        String newFullName = binding.tilFullName.getEditText().getText().toString();
        String newDob = binding.tilDob.getEditText().getText().toString();
        String newGender = binding.tilGender.getEditText().getText().toString();
        String newPhone = binding.tilPhone.getEditText().getText().toString();
        String newAddress = binding.tilAddress.getEditText().getText().toString();

        // Cập nhật object
        currentPatientData.setFullName(newFullName);
        currentPatientData.setDob(newDob);
        currentPatientData.setGender(newGender);
        currentPatientData.setPhone(formatPhoneNumber(newPhone)); // ⭐️ SỬA: Lưu SĐT đã chuẩn hóa
        currentPatientData.setAddress(newAddress);
        // currentPatientData.setAvatarUrl(...); // Cập nhật nếu có chọn ảnh mới

        // Gọi ViewModel để lưu
        viewModel.updatePatient(currentPatientData);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}