package com.example.medibookandroid.ui.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.medibookandroid.R;
import com.example.medibookandroid.databinding.FragmentLanguageSettingsBinding;

public abstract class BaseLanguageSettingsFragment extends Fragment {

    protected FragmentLanguageSettingsBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLanguageSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Xử lý nút Back
        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(view).navigateUp());

        // 2. Xử lý Logic chọn ngôn ngữ
        binding.rgLanguageOptions.setOnCheckedChangeListener((group, checkedId) -> {
            String selectedLanguage = "Không xác định";
            if (checkedId == R.id.rb_vietnamese) {
                selectedLanguage = "Tiếng Việt";
            } else if (checkedId == R.id.rb_english) {
                selectedLanguage = "English";
            }
            // Demo Toast
            showToast("Đã chọn: " + selectedLanguage);
        });

        // 3. Xử lý nút Lưu
        binding.btnSaveLanguage.setOnClickListener(v -> {
            // TODO: Thực hiện lưu Locale vào SharedPreferences và reload App
            showToast("Đã lưu ngôn ngữ (Demo)");
            Navigation.findNavController(view).navigateUp();
        });
    }

    // --- LOGIC ẨN/HIỆN BOTTOM NAV (Giống hệt BaseSettingsFragment) ---

    @Override
    public void onResume() {
        super.onResume();
        toggleBottomNav(false); // Ẩn
    }

    @Override
    public void onPause() {
        super.onPause();
        toggleBottomNav(true); // Hiện
    }

    private void toggleBottomNav(boolean isVisible) {
        if (getActivity() != null) {
            View bottomNav = getActivity().findViewById(getBottomNavigationId());
            if (bottomNav != null) {
                bottomNav.setVisibility(isVisible ? View.VISIBLE : View.GONE);
            }
        }
    }

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    // --- HÀM TRỪU TƯỢNG ---
    protected abstract int getBottomNavigationId();

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}