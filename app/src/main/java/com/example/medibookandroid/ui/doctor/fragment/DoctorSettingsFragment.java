package com.example.medibookandroid.ui.doctor.fragment;

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

import com.example.medibookandroid.R;
import com.example.medibookandroid.ui.help.HelpActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.medibookandroid.databinding.FragmentDoctorSettingsBinding;

public class DoctorSettingsFragment extends Fragment {

    private FragmentDoctorSettingsBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDoctorSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

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
        final NavController navController = Navigation.findNavController(view);

        binding.toolbar.setNavigationOnClickListener(v -> navController.navigateUp());

        binding.itemNotificationSetting.setOnClickListener(v -> {
            navController.navigate(R.id.action_doctorSettingsFragment_to_doctorNotificationSettingsFragment);
        });

        binding.itemLanguageSetting.setOnClickListener(v -> {
            navController.navigate(R.id.action_doctorSettingsFragment_to_doctorLanguageSettingsFragment);
        });

        binding.itemPasswordManager.setOnClickListener(v -> {
            navController.navigate(R.id.action_doctorSettingsFragment_to_doctorPasswordManagerFragment);
        });

        binding.itemDeleteAccount.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Xác nhận Xóa Tài khoản")
                    .setMessage("Hành động này không thể hoàn tác. Bạn có chắc chắn muốn xóa tài khoản?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        Toast.makeText(getContext(), "Chức năng Xóa Tài khoản đang phát triển", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                    .show();
        });
        // Ví dụ trong PatientSettingsFragment.java
        // THÊM DÒNG NÀY – MỞ MÀN HÌNH TRỢ GIÚP
        // THÊM DÒNG NÀY – MỞ MÀN HÌNH TRỢ GIÚP
        // NÚT TRỢ GIÚP TRÊN MÀN HÌNH HỒ SƠ
        binding.itemHelp.setOnClickListener(v ->
                startActivity(new Intent(requireActivity(), com.example.medibookandroid.ui.help.HelpActivity.class))
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}