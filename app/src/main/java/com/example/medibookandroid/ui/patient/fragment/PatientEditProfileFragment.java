package com.example.medibookandroid.ui.patient.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.medibookandroid.R;
import com.example.medibookandroid.data.model.Patient;
import com.example.medibookandroid.databinding.FragmentPatientEditProfileBinding;
import com.example.medibookandroid.ui.patient.viewmodel.PatientViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONObject;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PatientEditProfileFragment extends Fragment {
    // Hằng số nhận diện request khi mở thư viện ảnh
    private static final int PICK_IMAGE_REQUEST = 101;
    // Lưu hash url
    private final Map<String, String> uploadedImages = new HashMap<>();

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
        loadUploadedImagesFromPrefs();

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

        // Sửa trong hàm populateUi(Patient patient) Hoàn
        String gender = patient.getGender();
        if (gender != null) {
            if (gender.equals("Nam")) {
                binding.radioNam.setChecked(true);
            } else if (gender.equals("Nữ")) {
                binding.radioNu.setChecked(true);
            } else if (gender.equals("Khác")) {
                binding.radioKhac.setChecked(true);
            }
        }
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
            //Toast.makeText(getContext(), "Chức năng đổi avatar chưa được triển khai", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Chọn ảnh"), PICK_IMAGE_REQUEST);
        });

        binding.toolbar.setNavigationOnClickListener(v -> navController.popBackStack());
    }
    // Kiểm tra kích cỡ ảnh, >10MB sẽ lỗi
    private boolean isImageSizeValid(Uri imageUri, long maxSizeBytes) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
            if (inputStream == null) return false;
            int fileSize = inputStream.available();
            inputStream.close();
            return fileSize <= maxSizeBytes;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();

            // Kiểm tra kích thước
            long maxSize = 10 * 1024 * 1024; // 10 MB
            if (!isImageSizeValid(imageUri, maxSize)) {
                Toast.makeText(getContext(), "Ảnh quá lớn. Vui lòng chọn ảnh dưới 10 MB.", Toast.LENGTH_SHORT).show();
                return;
            }
            // Tính hash MD5
            String hash = getImageHash(imageUri);
            if (hash == null) {
                Toast.makeText(getContext(), "Không thể xử lý ảnh.", Toast.LENGTH_SHORT).show();
                return;
            }
            // Kiểm tra xem đã upload chưa
            if (uploadedImages.containsKey(hash)) {
                String existingUrl = uploadedImages.get(hash);
                currentPatientData.setAvatarUrl(existingUrl);
                Glide.with(requireContext())
                        .load(existingUrl)
                        .placeholder(R.drawable.logo2)
                        .circleCrop()
                        .into(binding.ivUserAvatar);
            } else {
                uploadImageToCloudinary(imageUri, hash);
            }
        }
    }
    // Hàm tính Hash ảnh
    private String getImageHash(Uri imageUri) {
        try {
            InputStream is = requireContext().getContentResolver().openInputStream(imageUri);
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
            is.close();
            byte[] hashBytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void uploadImageToCloudinary(Uri imageUri, String hash) {
        if (imageUri == null) return;
        if (binding != null) {
            binding.progressAvatarUpload.setVisibility(View.VISIBLE);
            binding.ivUserAvatar.setAlpha(0.4f);
        }
        MediaManager.get().upload(imageUri)
                .unsigned("Medibook_img") // tên upload preset
                .option("public_id", hash)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {}
                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}
                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        if (!isAdded()) return;
                        String imageUrl = (String) resultData.get("secure_url");
                        uploadedImages.put(hash, imageUrl);
                        currentPatientData.setAvatarUrl(imageUrl);
                        saveUploadedImagesToPrefs();
                        Glide.with(requireContext())
                                .load(imageUrl)
                                .placeholder(R.drawable.logo2)
                                .circleCrop()
                                .into(binding.ivUserAvatar);
                        binding.progressAvatarUpload.setVisibility(View.GONE);
                        binding.ivUserAvatar.setAlpha(1f);
                        Toast.makeText(getContext(), "Tải ảnh lên thành công", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        if (!isAdded()) return;
                        Toast.makeText(getContext(), "Upload thất bại: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                        binding.progressAvatarUpload.setVisibility(View.GONE);
                        binding.ivUserAvatar.setAlpha(1f);
                    }
                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                }).dispatch();
    }
    // SharedPreferences helpers - lưu theo UID
    private void saveUploadedImagesToPrefs() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (currentPatientData == null || user == null) return;

        SharedPreferences prefs = requireContext().getSharedPreferences("avatar_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        try {
            JSONObject json = new JSONObject(uploadedImages);
            String key = "uploaded_images_" + user.getUid(); // riêng theo UID
            editor.putString(key, json.toString());
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadUploadedImagesFromPrefs() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        SharedPreferences prefs = requireContext().getSharedPreferences("avatar_prefs", Context.MODE_PRIVATE);
        String key = "uploaded_images_" + user.getUid(); // riêng theo UID
        String jsonStr = prefs.getString(key, "{}");
        try {
            JSONObject json = new JSONObject(jsonStr);
            uploadedImages.clear();
            Iterator<String> keys = json.keys();
            while (keys.hasNext()) {
                String k = keys.next();
                uploadedImages.put(k, json.getString(k));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ⭐️ BẮT ĐẦU THÊM MỚI: Logic Validate ⭐️
    /**
     * Kiểm tra các trường input
     */
    private boolean validateInput() {
        // Reset lỗi
        binding.tilFullName.setError(null);
        binding.tilDob.setError(null);
        binding.tvGenderError.setVisibility(View.GONE);
        binding.tilPhone.setError(null);
        binding.tilAddress.setError(null);

        boolean valid = true;

        String name = binding.tilFullName.getEditText().getText().toString().trim();
        String dob = binding.tilDob.getEditText().getText().toString().trim();
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
        if (address.isEmpty()) {
            binding.tilAddress.setError("Địa chỉ không được để trống");
            valid = false;
        }

        // 4. Kiểm tra Giới tính
        if (binding.radioGroupGender.getCheckedRadioButtonId() == -1) {
            // -1 có nghĩa là không có nút nào được chọn
            binding.tvGenderError.setVisibility(View.VISIBLE); // Hiển thị lỗi
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
        int selectedGenderId = binding.radioGroupGender.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = (RadioButton) getView().findViewById(selectedGenderId);
        String newGender = selectedRadioButton.getText().toString(); // Sẽ là "Nam", "Nữ" hoặc "Khác"
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