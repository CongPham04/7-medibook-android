package com.example.medibookandroid.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
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
import com.example.medibookandroid.ui.common.LoadingDialog; // ⭐️ THÊM
import com.example.medibookandroid.ui.doctor.DoctorMainActivity;
import com.example.medibookandroid.ui.patient.PatientMainActivity;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private NavController navController;
    private AuthViewModel viewModel;

    private LoadingDialog loadingDialog; // ⭐️ THÊM

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

        loadingDialog = new LoadingDialog(); // ⭐️ THÊM: Khởi tạo Dialog

        binding.btnLogin.setOnClickListener(v -> {
            if (validateInput()) { // ⭐️ SỬA: Tách hàm validate
                String email = binding.tilEmail.getEditText().getText().toString().trim();
                String password = binding.tilPassword.getEditText().getText().toString();

                // ⭐️ SỬA: Không show dialog ở đây
                viewModel.login(email, password);
            }
        });

        setupObservers(); // ⭐️ SỬA: Gọi setupObservers

        binding.tvSignUp.setOnClickListener(v ->
                navController.navigate(R.id.action_loginFragment_to_registerFragment));
    }

    private boolean validateInput() {
        // (Đây là logic validate bạn đã viết)
        String email = binding.tilEmail.getEditText().getText().toString().trim();
        String password = binding.tilPassword.getEditText().getText().toString();

        if (email.isEmpty()) {
            binding.tilEmail.setError("Email không được để trống");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Email không hợp lệ");
            return false;
        }
        if (password.isEmpty()) {
            binding.tilPassword.setError("Mật khẩu không được để trống");
            return false;
        }
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);
        return true;
    }

    // ⭐️ SỬA: Đổi tên hàm
    private void setupObservers() {

        // ⭐️ THÊM: Observer cho trạng thái loading
        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null && isLoading) {
                // Hiển thị dialog (tránh crash nếu fragment đã đóng)
                if (!loadingDialog.isAdded() && getChildFragmentManager() != null) {
                    loadingDialog.show(getChildFragmentManager(), "loading");
                }
            } else {
                // Ẩn dialog
                if (loadingDialog.isAdded()) {
                    loadingDialog.dismiss();
                }
            }
        });

        // Observer cho kết quả đăng nhập (thành công)
        viewModel.getLoginUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                // (loading đã tự động ẩn bởi observer ở trên)
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

        // Observer cho lỗi
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                // (loading đã tự động ẩn bởi observer ở trên)
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}