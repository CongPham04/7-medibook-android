package com.example.medibookandroid.ui.auth;

import android.os.Bundle;
import android.util.Patterns; // Import
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
import com.example.medibookandroid.R;
import com.example.medibookandroid.databinding.FragmentRegisterBinding;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private NavController navController;
    private AuthViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        binding.cbTerms.setOnCheckedChangeListener((b, c) -> binding.btnRegister.setEnabled(c));

        binding.btnRegister.setOnClickListener(v -> {
            if (validateInput()) {
                String name = binding.tilFullName.getEditText().getText().toString().trim();
                String email = binding.tilEmail.getEditText().getText().toString().trim(); // Lấy email
                String phone = binding.tilPhone.getEditText().getText().toString().trim();
                String password = binding.tilPassword.getEditText().getText().toString();

                // Gọi thẳng viewModel.register
                viewModel.register(email, password, name, phone);
                observeRegisterResult();
            }
        });

        binding.toolbar.setNavigationOnClickListener(v -> navController.popBackStack());
        binding.tvLogin.setOnClickListener(v -> navController.navigate(R.id.action_registerFragment_to_loginFragment));
    }

    // Quan sát kết quả đăng ký
    private void observeRegisterResult() {
        viewModel.getRegisterSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(getContext(), "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                navController.navigate(R.id.action_registerFragment_to_loginFragment);
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(),
                msg -> Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show());
    }

    private boolean validateInput() {
        binding.tilFullName.setError(null);
        binding.tilEmail.setError(null); // Thêm
        binding.tilPhone.setError(null);
        binding.tilPassword.setError(null);
        boolean valid = true;

        String name = binding.tilFullName.getEditText().getText().toString().trim();
        String email = binding.tilEmail.getEditText().getText().toString().trim();
        String phone = binding.tilPhone.getEditText().getText().toString().trim();
        String password = binding.tilPassword.getEditText().getText().toString();

        if (name.isEmpty()) {
            binding.tilFullName.setError("Họ tên không được để trống");
            valid = false;
        }

        if (email.isEmpty()) {
            binding.tilEmail.setError("Email không được để trống");
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Email không hợp lệ");
            valid = false;
        }

        if (phone.isEmpty()) {
            binding.tilPhone.setError("Số điện thoại không được để trống");
            valid = false;
        }

        if (password.isEmpty()) {
            binding.tilPassword.setError("Mật khẩu không được để trống");
            valid = false;
        } else if (password.length() < 6) {
            binding.tilPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            valid = false;
        }

        return valid;
    }

    // KHÔNG CẦN formatPhoneNumber nữa

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}