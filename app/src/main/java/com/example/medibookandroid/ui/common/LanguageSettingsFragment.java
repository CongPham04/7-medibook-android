package com.example.medibookandroid.ui.common;

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
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;


// 1. IMPORT BottomNavigationView
import com.example.medibookandroid.MainActivity;
import com.example.medibookandroid.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.medibookandroid.databinding.FragmentLanguageSettingsBinding;

public class LanguageSettingsFragment extends Fragment {

    private FragmentLanguageSettingsBinding binding;
    private NavController navController;
    // Hằng số để lưu trữ cài đặt, giúp code sạch và an toàn hơn
    public static final String PREFS_NAME = "AppSettings";
    public static final String PREF_LANGUAGE_KEY = "AppLanguage";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLanguageSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        // Xử lý nút quay lại trên Toolbar
        binding.toolbar.setNavigationOnClickListener(v -> navController.navigateUp());

        // 1. Tự động chọn RadioButton dựa trên ngôn ngữ hiện tại của app
        loadCurrentLanguageSelection();

        // 2. Kích hoạt nút "Lưu thay đổi" với logic thực tế
        binding.btnSaveLanguage.setOnClickListener(v -> {
            // Lấy mã ngôn ngữ được chọn ('vi' hoặc 'en') từ RadioGroup
            String selectedLang = binding.rbVietnamese.isChecked() ? "vi" : "en";
            String currentLang = getCurrentAppLanguage();

            // Chỉ thực hiện hành động nếu người dùng chọn một ngôn ngữ khác
            if (!selectedLang.equals(currentLang)) {
                applyAndRestart(selectedLang);
            } else {
                showToast("Bạn đã chọn ngôn ngữ hiện tại.");
            }
        });
    }

    /**
     * Đọc ngôn ngữ hiện tại của ứng dụng và check vào RadioButton tương ứng.
     */
    private void loadCurrentLanguageSelection() {
        String currentLang = getCurrentAppLanguage();
        if ("vi".equals(currentLang)) {
            binding.rbVietnamese.setChecked(true);
        } else {
            // Mặc định là tiếng Anh nếu không phải Tiếng Việt
            binding.rbEnglish.setChecked(true);
        }
    }

    /**
     * Lấy mã ngôn ngữ hiện tại của ứng dụng ('vi' hoặc 'en').
     */
    private String getCurrentAppLanguage() {
        LocaleListCompat locales = AppCompatDelegate.getApplicationLocales();
        if (locales.isEmpty() || locales.get(0) == null) {
            return "en"; // Mặc định là 'en' nếu chưa được đặt
        }
        return locales.get(0).getLanguage();
    }



    /**
     * Lưu lựa chọn mới và khởi động lại ứng dụng để áp dụng thay đổi.
     * @param langCode Mã ngôn ngữ mới ("vi" hoặc "en").
     */
    private void applyAndRestart(String langCode) {
        if (getContext() == null) return; // Kiểm tra an toàn

        // 1. Lưu ngôn ngữ mới vào bộ nhớ điện thoại (SharedPreferences)
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(PREF_LANGUAGE_KEY, langCode).apply();

        // 2. Áp dụng ngôn ngữ mới cho ứng dụng ngay lập tức
        LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(langCode);
        AppCompatDelegate.setApplicationLocales(appLocale);

        // 3. Khởi động lại ứng dụng để thay đổi có hiệu lực trên mọi màn hình
        Intent intent = new Intent(requireContext(), MainActivity.class); // ❗️ QUAN TRỌNG
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // Kết thúc activity hiện tại để người dùng không thể quay lại
        requireActivity().finish();
    }


    // 2. THÊM onResume ĐỂ ẨN BOTTOM NAV
    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.patient_bottom_nav); // Đảm bảo ID này đúng
            if (bottomNav != null) {
                bottomNav.setVisibility(View.GONE);
            }
        }
    }

    // 3. THÊM onPause ĐỂ HIỆN LẠI BOTTOM NAV
    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() != null) {
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.patient_bottom_nav); // Đảm bảo ID này đúng
            if (bottomNav != null) {
                bottomNav.setVisibility(View.VISIBLE);
            }
        }
    }
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//        final NavController navController = Navigation.findNavController(view);
//
//        // Xử lý nút quay lại trên Toolbar
//        binding.toolbar.setNavigationOnClickListener(v -> navController.navigateUp());
//
//        // Logic demo khi chọn ngôn ngữ
//        binding.rgLanguageOptions.setOnCheckedChangeListener((group, checkedId) -> {
//            String selectedLanguage;
//            if (checkedId == R.id.rb_vietnamese) {
//                selectedLanguage = "Tiếng Việt";
//            } else if (checkedId == R.id.rb_english) {
//                selectedLanguage = "English";
//            } else {
//                selectedLanguage = "Không xác định";
//            }
//            showToast("Đã chọn ngôn ngữ: " + selectedLanguage);
//            // TODO: Thêm logic thay đổi ngôn ngữ ứng dụng thực tế
//        });
//
//        // Xử lý nút Lưu (nếu bạn bỏ comment nút này trong XML)
//        /*
//        binding.btnSaveLanguage.setOnClickListener(v -> {
//            int selectedId = binding.rgLanguageOptions.getCheckedRadioButtonId();
//            // TODO: Lưu cài đặt ngôn ngữ và áp dụng thay đổi
//            showToast("Đã lưu cài đặt ngôn ngữ");
//            navController.navigateUp(); // Quay lại màn hình trước
//        });
//        */
//    }

    private void showToast(String message) {
        if(getContext() != null) { // Thêm kiểm tra null cho context
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}