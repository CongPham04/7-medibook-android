package com.example.medibookandroid.ui.common;

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

// 1. IMPORT BottomNavigationView
import com.example.medibookandroid.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.medibookandroid.databinding.FragmentLanguageSettingsBinding;

public class LanguageSettingsFragment extends Fragment {

    private FragmentLanguageSettingsBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLanguageSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final NavController navController = Navigation.findNavController(view);

        // Xử lý nút quay lại trên Toolbar
        binding.toolbar.setNavigationOnClickListener(v -> navController.navigateUp());

        // Logic demo khi chọn ngôn ngữ
        binding.rgLanguageOptions.setOnCheckedChangeListener((group, checkedId) -> {
            String selectedLanguage;
            if (checkedId == R.id.rb_vietnamese) {
                selectedLanguage = "Tiếng Việt";
            } else if (checkedId == R.id.rb_english) {
                selectedLanguage = "English";
            } else {
                selectedLanguage = "Không xác định";
            }
            showToast("Đã chọn ngôn ngữ: " + selectedLanguage);
            // TODO: Thêm logic thay đổi ngôn ngữ ứng dụng thực tế
        });

        // Xử lý nút Lưu (nếu bạn bỏ comment nút này trong XML)
        /*
        binding.btnSaveLanguage.setOnClickListener(v -> {
            int selectedId = binding.rgLanguageOptions.getCheckedRadioButtonId();
            // TODO: Lưu cài đặt ngôn ngữ và áp dụng thay đổi
            showToast("Đã lưu cài đặt ngôn ngữ");
            navController.navigateUp(); // Quay lại màn hình trước
        });
        */
    }

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