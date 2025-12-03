package com.example.medibookandroid.ui.doctor.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.medibookandroid.R;
import com.example.medibookandroid.data.model.Doctor;
import com.example.medibookandroid.databinding.FragmentDoctorEditProfileBinding;
import com.example.medibookandroid.ui.doctor.viewmodel.DoctorViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONObject;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DoctorEditProfileFragment extends Fragment {
    // Hằng số nhận diện request khi mở thư viện ảnh
    private static final int PICK_IMAGE_REQUEST = 101;
    // Lưu hash url
    private final Map<String, String> uploadedImages = new HashMap<>();
    private FragmentDoctorEditProfileBinding binding;
    private DoctorViewModel viewModel;
    private NavController navController;
    private Doctor currentDoctorData; // Để giữ object doctor

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDoctorEditProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    // (Các hàm onResume, onPause giữ nguyên)
    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            navController = Navigation.findNavController(requireActivity(), R.id.doctor_nav_host_fragment);
        } catch (Exception e) {
            navController = Navigation.findNavController(view);
        }

        viewModel = new ViewModelProvider(this).get(DoctorViewModel.class);
        loadUploadedImagesFromPrefs();
        // 1. Tải dữ liệu
        loadProfileData();

        // 2. Lắng nghe dữ liệu
        setupObservers();

        // 3. Cài đặt nút
        setupClickListeners();
    }

    private void loadProfileData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            viewModel.loadDoctor(currentUser.getUid());
        } else {
            Toast.makeText(getContext(), "Lỗi xác thực người dùng.", Toast.LENGTH_SHORT).show();
            navController.popBackStack();
        }
    }

    private void setupObservers() {
        // 1. Lắng nghe thông tin Doctor
        viewModel.getDoctor().observe(getViewLifecycleOwner(), doctor -> {
            if (doctor != null) {
                this.currentDoctorData = doctor; // Lưu lại object
                populateUi(doctor); // Điền thông tin vào form
            }
        });

        // 2. Lắng nghe kết quả LƯU
        viewModel.getUpdateDoctorStatus().observe(getViewLifecycleOwner(), success -> {
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
     * Điền dữ liệu từ Doctor object vào các ô EditText
     */
    private void populateUi(Doctor doctor) {
        if (getContext() != null && doctor.getAvatarUrl() != null && !doctor.getAvatarUrl().isEmpty()) {
            Glide.with(getContext())
                    .load(doctor.getAvatarUrl())
                    .placeholder(R.drawable.logo2)
                    .circleCrop()
                    .into(binding.ivDoctorAvatar);
        }

        binding.tilFullName.getEditText().setText(doctor.getFullName());
        binding.tilSpecialty.getEditText().setText(doctor.getSpecialty());
        binding.tilQualification.getEditText().setText(doctor.getQualifications());
        binding.tilWorkplace.getEditText().setText(doctor.getWorkplace());
        binding.tilPhone.getEditText().setText(doctor.getPhone());
        binding.tilDescription.getEditText().setText(doctor.getAbout());
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
                currentDoctorData.setAvatarUrl(existingUrl);
                Glide.with(requireContext())
                        .load(existingUrl)
                        .placeholder(R.drawable.logo2)
                        .circleCrop()
                        .into(binding.ivDoctorAvatar);
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
    // Tải ảnh lên Cloudinary
    private void uploadImageToCloudinary(Uri imageUri, String hash) {
        if (imageUri == null) return; // return luôn tránh crash khi không chọn ảnh
        if (binding != null) {
            binding.progressAvatarUpload.setVisibility(View.VISIBLE);
            binding.ivDoctorAvatar.setAlpha(0.4f); // làm mờ avatar khi đang upload
        }
        MediaManager.get().upload(imageUri)
                .unsigned("Medibook_img")
                .option("public_id", hash)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                    }
                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                    }
                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        // Kiểm tra fragment còn attach và binding != null
                        if (!isAdded() || binding == null) return;
                        String imageUrl = (String) resultData.get("secure_url");
                        currentDoctorData.setAvatarUrl(imageUrl);
                        uploadedImages.put(hash, imageUrl);
                        saveUploadedImagesToPrefs();
                        // Cập nhật avatar
                        Glide.with(requireContext())
                                .load(imageUrl)
                                .placeholder(R.drawable.logo2)
                                .circleCrop()
                                .into(binding.ivDoctorAvatar);
                        binding.progressAvatarUpload.setVisibility(View.GONE);
                        binding.ivDoctorAvatar.setAlpha(1f);
                        Toast.makeText(getContext(), "Tải ảnh lên thành công", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        if (!isAdded()) return;
                        Toast.makeText(getContext(), "Upload thất bại: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                        binding.progressAvatarUpload.setVisibility(View.GONE);
                        binding.ivDoctorAvatar.setAlpha(1f);
                    }
                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                    }
                })
                .dispatch();
    }
    private void saveUploadedImagesToPrefs() {
        if (currentDoctorData == null) return;
        SharedPreferences prefs = requireContext().getSharedPreferences("avatar_prefs", getContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        try {
            JSONObject json = new JSONObject(uploadedImages);
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) editor.putString("uploaded_images_" + user.getUid(), json.toString());
            editor.apply();
        } catch (Exception e) { e.printStackTrace(); }
    }
    private void loadUploadedImagesFromPrefs() {
        SharedPreferences prefs = requireContext().getSharedPreferences("avatar_prefs", getContext().MODE_PRIVATE);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String jsonStr = "{}";
        if (user != null) jsonStr = prefs.getString("uploaded_images_" + user.getUid(), "{}");
        try {
            JSONObject json = new JSONObject(jsonStr);
            uploadedImages.clear();
            Iterator<String> keys = json.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                uploadedImages.put(key, json.getString(key));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
    // ⭐️ BẮT ĐẦU THÊM MỚI: Logic Validate ⭐️
    /**
     * Kiểm tra các trường input
     */
    private boolean validateInput() {
        // Reset lỗi
        binding.tilFullName.setError(null);
        binding.tilSpecialty.setError(null);
        binding.tilQualification.setError(null);
        binding.tilWorkplace.setError(null);
        binding.tilPhone.setError(null);
        binding.tilDescription.setError(null);

        boolean valid = true;

        String name = binding.tilFullName.getEditText().getText().toString().trim();
        String specialty = binding.tilSpecialty.getEditText().getText().toString().trim();
        String qual = binding.tilQualification.getEditText().getText().toString().trim();
        String workplace = binding.tilWorkplace.getEditText().getText().toString().trim();
        String phone = binding.tilPhone.getEditText().getText().toString().trim();
        String about = binding.tilDescription.getEditText().getText().toString().trim();

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
        if (specialty.isEmpty()) {
            binding.tilSpecialty.setError("Chuyên khoa không được để trống");
            valid = false;
        }
        if (qual.isEmpty()) {
            binding.tilQualification.setError("Bằng cấp không được để trống");
            valid = false;
        }
        if (workplace.isEmpty()) {
            binding.tilWorkplace.setError("Nơi công tác không được để trống");
            valid = false;
        }
        if (about.isEmpty()) {
            binding.tilDescription.setError("Mô tả không được để trống");
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
     * Lấy dữ liệu từ form, cập nhật Doctor object và gọi ViewModel
     */
    private void performSave() { // ⭐️ SỬA: Đổi tên hàm
        if (currentDoctorData == null) {
            Toast.makeText(getContext(), "Đang tải dữ liệu, vui lòng chờ...", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnSaveProfile.setEnabled(false);
        binding.btnSaveProfile.setText("Đang lưu...");

        // Lấy dữ liệu mới từ Form
        String newFullName = binding.tilFullName.getEditText().getText().toString();
        String newSpecialty = binding.tilSpecialty.getEditText().getText().toString();
        String newQual = binding.tilQualification.getEditText().getText().toString();
        String newWorkplace = binding.tilWorkplace.getEditText().getText().toString();
        String newPhone = binding.tilPhone.getEditText().getText().toString();
        String newAbout = binding.tilDescription.getEditText().getText().toString();

        // Cập nhật object
        currentDoctorData.setFullName(newFullName);
        currentDoctorData.setSpecialty(newSpecialty);
        currentDoctorData.setQualifications(newQual);
        currentDoctorData.setWorkplace(newWorkplace);
        currentDoctorData.setPhone(formatPhoneNumber(newPhone)); // ⭐️ SỬA: Lưu SĐT đã chuẩn hóa
        currentDoctorData.setAbout(newAbout);
        // currentDoctorData.setAvatarUrl(...); // Cập nhật nếu có chọn ảnh mới

        // Gọi ViewModel để lưu
        viewModel.updateDoctor(currentDoctorData);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}