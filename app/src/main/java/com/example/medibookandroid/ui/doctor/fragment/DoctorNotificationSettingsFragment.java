package com.example.medibookandroid.ui.doctor.fragment;

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

import com.example.medibookandroid.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.medibookandroid.databinding.FragmentDoctorNotificationSettingsBinding;

public class DoctorNotificationSettingsFragment extends Fragment {

    private FragmentDoctorNotificationSettingsBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDoctorNotificationSettingsBinding.inflate(inflater, container, false);
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

        binding.switchVibrate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            showToast("Thông báo rung: " + (isChecked ? "Bật" : "Tắt"));
        });

        binding.switchSound.setOnCheckedChangeListener((buttonView, isChecked) -> {
            showToast("Thông báo âm thanh: " + (isChecked ? "Bật" : "Tắt"));
        });

        binding.switchGeneralNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            showToast("Thông báo chung: " + (isChecked ? "Bật" : "Tắt"));
        });
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