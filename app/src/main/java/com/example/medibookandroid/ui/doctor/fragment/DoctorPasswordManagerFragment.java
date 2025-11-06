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
import com.example.medibookandroid.databinding.FragmentDoctorPasswordManagerBinding;

public class DoctorPasswordManagerFragment extends Fragment {

    private FragmentDoctorPasswordManagerBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDoctorPasswordManagerBinding.inflate(inflater, container, false);
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

        binding.btnChangePassword.setOnClickListener(v -> {
            String oldPassword = binding.tilCurrentPassword.getEditText().getText().toString();
            String newPassword = binding.tilNewPassword.getEditText().getText().toString();
            String confirmPassword = binding.tilConfirmNewPassword.getEditText().getText().toString();

            binding.tilCurrentPassword.setError(null);
            binding.tilNewPassword.setError(null);
            binding.tilConfirmNewPassword.setError(null);

            boolean isValid = true;

            if (oldPassword.isEmpty()){
                binding.tilCurrentPassword.setError("Mật khẩu cũ không được để trống");
                isValid = false;
            }
            if (newPassword.isEmpty()) {
                binding.tilNewPassword.setError("Mật khẩu mới không được để trống");
                isValid = false;
            }
            if (confirmPassword.isEmpty()) {
                binding.tilConfirmNewPassword.setError("Xác nhận mật khẩu không được để trống");
                isValid = false;
            }
            if (!newPassword.equals(confirmPassword) && !newPassword.isEmpty() && !confirmPassword.isEmpty()) {
                binding.tilConfirmNewPassword.setError("Mật khẩu xác nhận không khớp");
                isValid = false;
            }

            if(isValid) {
                showToast("Đã lưu mật khẩu mới (Demo)");
                navController.navigateUp();
            }
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