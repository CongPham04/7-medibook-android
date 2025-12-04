package com.example.medibookandroid.ui.doctor.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate; // ⭐ THÊM MỚI
import androidx.core.os.LocaleListCompat;      // ⭐ THÊM MỚI
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.medibookandroid.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.medibookandroid.databinding.FragmentDoctorLanguageSettingsBinding;

public class DoctorLanguageSettingsFragment extends Fragment {

    private FragmentDoctorLanguageSettingsBinding binding;

    // ⭐ THÊM MỚI: Khai báo các hằng số để lưu trữ, giúp code sạch sẽ và tái sử dụng
    public static final String PREFS_NAME = "AppSettings";
    public static final String PREF_LANGUAGE_KEY = "AppLanguage";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDoctorLanguageSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final NavController navController = Navigation.findNavController(view);

        binding.toolbar.setNavigationOnClickListener(v -> navController.navigateUp());

        // ⭐ THÊM MỚI: Tải và hiển thị ngôn ngữ hiện tại lên RadioButton
        loadCurrentLanguage();

        // Xóa bỏ listener không cần thiết này để tránh hiển thị Toast mỗi khi chọn
        // binding.rgLanguageOptions.setOnCheckedChangeListener(...);

        // ⭐ THAY ĐỔI: Sửa lại hoàn toàn logic của nút Lưu
        binding.btnSaveLanguage.setOnClickListener(v -> {
            applyAndRestart(); // Gọi hàm xử lý chính
        });
    }

    /**
     * ⭐ HÀM MỚI: Đọc ngôn ngữ hiện tại của app và check vào RadioButton tương ứng
     */
    private void loadCurrentLanguage() {
        String currentLang = getCurrentAppLanguage();
        if ("vi".equals(currentLang)) {
            binding.rbVietnamese.setChecked(true);
        } else {
            binding.rbEnglish.setChecked(true);
        }
    }

    /**
     * ⭐ HÀM MỚI: Lấy mã ngôn ngữ hiện tại đang được áp dụng
     */
    private String getCurrentAppLanguage() {
        LocaleListCompat locales = AppCompatDelegate.getApplicationLocales();
        if (locales.isEmpty() || locales.get(0) == null) {
            // Mặc định là 'vi' nếu chưa có gì được cài đặt
            // Giá trị này phải khớp với giá trị mặc định trong MyApplication.java
            return "vi";
        }
        return locales.get(0).getLanguage(); // Trả về "vi" hoặc "en"
    }

    /**
     * ⭐ HÀM MỚI: Hàm xử lý cốt lõi - Lưu ngôn ngữ và khởi động lại ứng dụng
     */
    private void applyAndRestart() {
        // Bước 1: Xác định ngôn ngữ người dùng đã chọn
        String selectedLangCode = binding.rbVietnamese.isChecked() ? "vi" : "en";

        // Bước 2: (Tùy chọn) Nếu người dùng không thay đổi gì, chỉ cần thoát
        if (selectedLangCode.equals(getCurrentAppLanguage())) {
            Navigation.findNavController(requireView()).navigateUp();
            return;
        }

        // Bước 3: Lưu lựa chọn ngôn ngữ mới vào bộ nhớ (SharedPreferences)
        // requireActivity() để đảm bảo context không bị null
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(PREF_LANGUAGE_KEY, selectedLangCode).apply();

        // Bước 4: Áp dụng ngôn ngữ mới cho toàn bộ ứng dụng
        LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(selectedLangCode);
        AppCompatDelegate.setApplicationLocales(appLocale);

        // Bước 5: ✅ KHỞI ĐỘNG LẠI ACTIVITY HIỆN TẠI ĐỂ ÁP DỤNG NGÔN NGỮ
// Bằng cách này, bạn sẽ không bị đăng xuất.
        Intent intent = requireActivity().getIntent(); // Lấy Intent đã khởi chạy Activity này
        requireActivity().finish();                    // Đóng Activity hiện tại
        startActivity(intent);                         // Bắt đầu lại nó với Intent cũ

    }

    // Các hàm onResume, onPause, showToast, onDestroyView giữ nguyên như cũ...

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

    private void showToast(String message) {
        if(getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
