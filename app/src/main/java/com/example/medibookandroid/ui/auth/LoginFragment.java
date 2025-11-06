package com.example.medibookandroid.ui.auth;

import android.content.Intent;
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
import com.example.medibookandroid.data.model.User;
import com.example.medibookandroid.databinding.FragmentLoginBinding;
import com.example.medibookandroid.ui.doctor.DoctorMainActivity;
import com.example.medibookandroid.ui.patient.PatientMainActivity;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private NavController navController;
    private AuthViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        binding.btnLogin.setOnClickListener(v -> {
            // Lấy email, không phải SĐT
            String email = binding.tilEmail.getEditText().getText().toString().trim(); // SỬA
            String password = binding.tilPassword.getEditText().getText().toString();

            if (email.isEmpty()) { // SỬA
                binding.tilEmail.setError("Email không được để trống"); // SỬA
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { // SỬA
                binding.tilEmail.setError("Email không hợp lệ"); // SỬA
                return;
            }
            if (password.isEmpty()) {
                binding.tilPassword.setError("Mật khẩu không được để trống");
                return;
            }

            // Xóa lỗi
            binding.tilEmail.setError(null); // SỬA
            binding.tilPassword.setError(null);

            // Gọi viewModel.login với email
            viewModel.login(email, password); // SỬA
            observeLoginResult();
        });

        binding.tvSignUp.setOnClickListener(v ->
                navController.navigate(R.id.action_loginFragment_to_registerFragment));

        // (Xử lý Quên mật khẩu - chưa implement)
        // binding.tvForgotPassword.setOnClickListener(...);
    }

    private void observeLoginResult() {
        // Hàm này đã đúng logic từ trước, vì nó observe 'loginUser'
        viewModel.getLoginUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                Toast.makeText(getContext(),
                        "Đăng nhập thành công với vai trò: " + user.getRole(),
                        Toast.LENGTH_SHORT).show();

                if ("patient".equalsIgnoreCase(user.getRole())) {
                    startActivity(new Intent(getContext(), PatientMainActivity.class));
                    requireActivity().finish();
                } else if ("doctor".equalsIgnoreCase(user.getRole())) {
                    startActivity(new Intent(getContext(), DoctorMainActivity.class));
                    requireActivity().finish();
                } else {
                    Toast.makeText(getContext(), "Role không xác định", Toast.LENGTH_SHORT).show();
                }
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(),
                msg -> Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show());
    }

    // KHÔNG CẦN formatPhoneNumber nữa

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}